package cn.bctools.auth.config;

import cn.bctools.auth.component.UserInfoComponent;
import cn.bctools.auth.component.other.Oauth2OtherAuthenticationConverter;
import cn.bctools.auth.component.other.OtherAuthenticationProvider;
import cn.bctools.auth.service.UserDetailsServiceImpl;
import cn.bctools.common.utils.R;
import cn.bctools.oauth2.config.JvsOAuth2AuthorizationServiceImpl;
import cn.bctools.redis.utils.RedisUtils;
import cn.bctools.web.utils.WebUtils;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * SpringSecurity配置
 *
 * @author guojing
 */
@Configuration
@AllArgsConstructor
public class WebSecurityConfig {

    UserDetailsServiceImpl userDetailsService;
    RegisteredClientRepository registeredClientRepository;
    AuthenticationSuccessHandler authenticationSuccessHandler;
    AuthenticationFailureHandler authenticationFailureHandler;
    UserInfoComponent userInfoComponent;
    RedisUtils redisUtils;
    PasswordEncoder passwordEncoder;


    @Bean
    OAuth2AuthorizationService oAuth2AuthorizationService(RedisUtils redisUtils) {
        return new JvsOAuth2AuthorizationServiceImpl(redisUtils);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain configure(HttpSecurity http, OAuth2AuthorizationService oAuth2AuthorizationService) throws Exception {
        http.headers().frameOptions().disable().and().csrf().disable().cors().disable();
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        http.requestMatcher(authorizationServerConfigurer.getEndpointsMatcher());
        http.apply(authorizationServerConfigurer
                .tokenEndpoint((tokenEndpoint) -> tokenEndpoint.accessTokenRequestConverter(new Oauth2OtherAuthenticationConverter())
                        .accessTokenResponseHandler(authenticationSuccessHandler)
                        .errorResponseHandler(authenticationFailureHandler)));
        http.exceptionHandling().authenticationEntryPoint((request, response, authException) -> WebUtils.write(R.failed(authException.getMessage()), response));
        DefaultSecurityFilterChain build = http.authorizeHttpRequests(authorizeRequests -> {
            // 自定义接口、端点暴露
            authorizeRequests.antMatchers("/wx/portal/**", "/oauth2/**", "/actuator/**", "/phone/**", "/just/**", "/wx/**", "/v3/**").permitAll();
            authorizeRequests.anyRequest().authenticated();
        }).apply(authorizationServerConfigurer
                // redis存储token的实现
                .authorizationService(oAuth2AuthorizationService)
                .authorizationServerSettings(AuthorizationServerSettings.builder().build()))
                .and().build();
        http.authenticationProvider(new OtherAuthenticationProvider(oAuth2AuthorizationService, userDetailsService, registeredClientRepository, redisUtils, passwordEncoder));
        return build;
    }

}
