package cn.bctools.oauth2.annotation;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.feign.annotation.EnableJvsFeignClients;
import cn.bctools.oauth2.config.ClientFeignConfig;
import cn.bctools.oauth2.config.JvsAdapter;
import cn.bctools.oauth2.config.JvsOAuth2AuthorizationServiceImpl;
import cn.bctools.oauth2.utils.AuthorityManagementUtils;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Administrator
 * 1、通过在 Spring Boot 主类上添加 @EnableFeignClients 注解，启用 Feign 客户端功能，使得 Feign 的相关注解及功能生效
 * 添加了cn.bctools.**.api下所有的名
 * 2、通过{@linkplain EnableWebSecurity}添加权限拦截,目前所有接口都是放开状态,由网关直接判断权限,如果有权限,直接请求微服务,没有权限时,直接退出
 * 3、{@linkplain JvsOAuth2AuthorizationServiceImpl} 通过前端请求的token在redis 获取用户对象信息
 * 4、当用于不需要获取当前用户时, 但需要通过api获取某个用户的详细信息或角色,部门等信息,可以通过{@linkplain AuthorityManagementUtils}或{@linkplain AuthUserServiceApi} 等api
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@EnableJvsFeignClients
@EnableWebSecurity
@Import({
        ClientFeignConfig.class,
        JvsAdapter.class,
        JvsOAuth2AuthorizationServiceImpl.class,
        AuthorityManagementUtils.class,
})
public @interface EnableJvsMgrResourceServer {

}
