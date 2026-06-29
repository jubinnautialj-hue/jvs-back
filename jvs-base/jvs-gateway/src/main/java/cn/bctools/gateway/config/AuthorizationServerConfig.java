package cn.bctools.gateway.config;

import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.gateway.filter.AuthGlobalFilter;
import cn.bctools.gateway.route.GatewayRouteDefinitionWriter;
import cn.bctools.gateway.utils.CacheUtils;
import cn.bctools.oauth2.config.JvsOAuth2AuthorizationServiceImpl;
import cn.bctools.redis.JvsMessageListenerAdapter;
import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler;


/**
 * 资源服务器配置
 *
 * @author guojing
 */
@Configuration
@Import(JvsOAuth2AuthorizationServiceImpl.class)
@AllArgsConstructor
public class AuthorizationServerConfig {

    JvsOAuth2AuthorizationServiceImpl oAuth2AuthorizationService;
    AuthorizationManager authorizationManager;
    JvsServerBearerTokenAuthenticationConverter jvsServerBearerTokenAuthenticationConverter;
    ReactiveRedisTemplate<String, Object> redisTemplate;


    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        JvsReactiveAuthenticationManager tokenAuthenticationManager = new JvsReactiveAuthenticationManager(oAuth2AuthorizationService, redisTemplate);
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(tokenAuthenticationManager);
        authenticationWebFilter.setServerAuthenticationConverter(jvsServerBearerTokenAuthenticationConverter);
        http.addFilterBefore(authenticationWebFilter, SecurityWebFiltersOrder.AUTHORIZATION);
        http.authorizeExchange()
                .anyExchange()
                .access(authorizationManager)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint((exchange, ex) -> new HttpStatusServerAccessDeniedHandler(HttpStatus.OK).handle(exchange, new AccessDeniedException(JSON.toJSONString(R.failed("没有权限")))))
                .accessDeniedHandler((exchange, denied) -> new HttpStatusServerAccessDeniedHandler(HttpStatus.OK).handle(exchange, new AccessDeniedException(JSON.toJSONString(R.failed("没有权限")))))
                .and()
                .csrf().disable();
        return http.build();
    }

    /**
     * 接受到网关信息发生变化后自动刷新数据缓存
     *
     * @return
     */
    @Bean("gatewayConfig")
    JvsMessageListenerAdapter gatewayConfig() {
        return new JvsMessageListenerAdapter() {
            @Override
            public void onMessage(String obj) {
                //清空缓存
                CacheUtils.cache.clear();
                AuthGlobalFilter.cache.clear();
                CacheUtils.pathCache.clear();
                //清空路由缓存
                GatewayRouteDefinitionWriter.clearRoute();
                //刷新路由
                SpringContextUtil.getApplicationContext().publishEvent(new RefreshRoutesEvent(this));
            }
        };
    }


}
