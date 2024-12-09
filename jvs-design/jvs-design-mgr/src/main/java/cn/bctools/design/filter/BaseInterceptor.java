package cn.bctools.design.filter;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.util.CurrentAppUtils;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;

/**
 *  @author jvs
 */
@Slf4j
@Accessors(chain = true)
public class BaseInterceptor implements HandlerInterceptor {
    JvsAppService jvsAppService;

    public BaseInterceptor(JvsAppService jvsAppService) {
        this.jvsAppService = jvsAppService;
    }

    /**
     * 应用id
     */
    private static final String APP_ID = "appId";
    private static final String UNDEFINED = "undefined";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 鉴权获取参数
        Map<String, Object> variablesAttribute = (Map<String, Object>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String appId = getVariablesAppId(variablesAttribute);
        if (UNDEFINED.equals(appId)) {
            InterceptorUtil.throwException(response.getOutputStream(), "appId为undefined");
            return Boolean.FALSE;
        }
        if (ObjectNull.isNotNull(appId)) {
            JvsApp jvsApp = jvsAppService.getAppById(appId);
            // 将当前应用的版本号设置到上下文(用于sql自动添加应用版本号查询)
            CurrentAppUtils.setApp(jvsApp);
        }
        return Boolean.TRUE;
    }

    /**
     * 从路径变量中获取应用id
     *
     * @param variablesAttribute 请求对象参数
     * @return 返回应用 id
     */
    private String getVariablesAppId(Map<String, Object> variablesAttribute) {
        String appId = null;
        if (MapUtils.isNotEmpty(variablesAttribute)) {
            appId = Optional.ofNullable(variablesAttribute.get(APP_ID)).map(String::valueOf).orElseGet(() -> null);
        }
        return appId;
    }
}
