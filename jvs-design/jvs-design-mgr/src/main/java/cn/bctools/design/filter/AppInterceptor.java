package cn.bctools.design.filter;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.permission.BasePermissionHandlerHandler;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.util.CurrentAppUtils;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author zhuxiaokang
 * 权限拦截过滤
 */
@Slf4j
@Accessors(chain = true)
public class AppInterceptor implements HandlerInterceptor {

    List<BasePermissionHandlerHandler> handlers = new ArrayList<>();

    /**
     * 添加处理器
     *
     * @param basePermissionHandlerHandler
     * @return
     */
    public AppInterceptor addHandler(BasePermissionHandlerHandler basePermissionHandlerHandler) {
        this.handlers.add(basePermissionHandlerHandler);
        return this;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
        String requestUri = request.getRequestURI();
        ServletOutputStream outputStream = response.getOutputStream();
        // 超级管理员直接放行
        UserDto userDto = UserCurrentUtils.getNullableUser();
        if (ObjectNull.isNotNull(userDto) && Boolean.TRUE.equals(userDto.getAdminFlag())) {
            return Boolean.TRUE;
        }
        JvsApp jvsApp = CurrentAppUtils.getApp();
        if (ObjectNull.isNotNull(jvsApp)) {
            // 鉴权获取参数
            Map<String, Object> variablesAttribute = (Map<String, Object>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            //获取所有的权限处理类，并直接进行处理转换
            for (BasePermissionHandlerHandler handler : handlers) {
                //执行检查
                try {
                    boolean check = handler.check(userDto, jvsApp.getId(), jvsApp, requestUri, variablesAttribute);
                    //如果为通过则放开
                    if (check) {
                        return true;
                    }
                } catch (BusinessException e) {
                    //直接将错误信息返回给前端
                    InterceptorUtil.throwException(outputStream, e.getMessage());
                    return false;
                }
            }
        } else {
            InterceptorUtil.throwException(outputStream, "模式不正确或没有权限");
            return Boolean.FALSE;
        }
        log.error("请求地址错误,可能需要修改接口:" + requestUri);
        InterceptorUtil.throwException(outputStream, "请求地址错误");
        return Boolean.FALSE;
    }


}
