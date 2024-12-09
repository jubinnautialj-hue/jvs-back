package cn.bctools.auth.interceptor;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.R;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;

/**
 * @author guojing
 */
@Slf4j
@Data
@Configuration
public class PlatformWebMvcConfig implements WebMvcConfigurer {

    static final String PLATFORM = "/platform/";
    /**
     * 租户ID
     */
    private static final String TENATNT_ID = "tenantId";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 应用权限拦截器
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                String uri = request.getRequestURI();

                Map<String, Object> variablesAttribute = (Map<String, Object>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                //如果是平台操作, 判断是否是平台管理,或租户管理员
                if (uri.startsWith(PLATFORM)) {
                    UserDto currentUser = UserCurrentUtils.getCurrentUser();
                    if (currentUser.getPlatformAdmin()) {
                        return Boolean.TRUE;
                    } else {
                        String variablesAppId = getVariablesAppId(variablesAttribute, TENATNT_ID);
                        //如果是管理员,租户也能匹配上, 则允许操作
                        if (currentUser.getAdminFlag() && currentUser.getTenantId().equals(variablesAppId)) {
                            return Boolean.TRUE;
                        } else {
                            ServletOutputStream outputStream = response.getOutputStream();
                            outputStream.write(JSONObject.toJSONString(R.failed("非管理员不能操作")).getBytes());
                            outputStream.flush();
                            outputStream.close();
                            return Boolean.FALSE;
                        }
                    }
                }
                return true;
            }
        }).addPathPatterns(PLATFORM);
    }

    /**
     * 从路径变量中获取应用id
     *
     * @param variablesAttribute
     * @return
     * @throws Exception
     */
    private String getVariablesAppId(Map<String, Object> variablesAttribute, String key) {
        String appId = null;
        if (MapUtils.isNotEmpty(variablesAttribute)) {
            appId = Optional.ofNullable(variablesAttribute.get(key)).map(String::valueOf).orElseGet(() -> null);
        }
        return appId;
    }
}
