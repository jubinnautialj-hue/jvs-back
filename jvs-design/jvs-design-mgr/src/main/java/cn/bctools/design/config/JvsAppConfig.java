package cn.bctools.design.config;

import cn.bctools.design.filter.AppInterceptor;
import cn.bctools.design.filter.BaseInterceptor;
import cn.bctools.design.permission.AppPermissionHandler;
import cn.bctools.design.permission.DesignPermissionHandler;
import cn.bctools.design.permission.ResourcePermissionHandler;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.project.service.JvsAppVersionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The type Jvs app config.
 * 应用接口的拦截器 用于判断应用，设计是否具有权限。或请求资源是否存在，如果不存在或权限不足时直接失败
 *
 * @author jvs
 */
@Slf4j
@Configuration
public class JvsAppConfig {
    /**
     * 添加权限检查
     *
     * @param appPermissionHandler      the app permission handler  应用权限拦截器
     * @param designPermissionHandler   the design permission handler  设计拦截器
     * @param resourcePermissionHandler the resource permission handler  资源拦截器， 主要判断是否存在
     * @return app interceptor
     */
    @Bean
    AppInterceptor appInterceptor(AppPermissionHandler appPermissionHandler, DesignPermissionHandler designPermissionHandler, ResourcePermissionHandler resourcePermissionHandler) {
        return new AppInterceptor()
                .addHandler(appPermissionHandler)
                .addHandler(designPermissionHandler)
                .addHandler(resourcePermissionHandler);
    }

    /**
     * Base interceptor base interceptor.
     *
     * @param jvsAppService the jvs app service
     * @return the base interceptor
     */
    @Bean
    BaseInterceptor baseInterceptor(JvsAppService jvsAppService, JvsAppVersionService jvsAppVersionService) {
        return new BaseInterceptor(jvsAppService, jvsAppVersionService);
    }
}
