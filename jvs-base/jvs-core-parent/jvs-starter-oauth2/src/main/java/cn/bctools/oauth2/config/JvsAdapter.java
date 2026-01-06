package cn.bctools.oauth2.config;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.oauth2.dto.CustomUser;
import feign.RequestInterceptor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import com.alibaba.ttl.threadpool.TtlExecutors;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The type Jvs adapter.
 *
 * @author Administrator
 */
@Slf4j
@Lazy(value = false)
@EnableWebSecurity
@AllArgsConstructor
public class JvsAdapter {

    /**
     * The User.
     */
    static final String USER = "user";


    /**
     * The O auth 2 authorization service.
     */
    @Autowired
    OAuth2AuthorizationService oAuth2AuthorizationService;

    /**
     * Security filter chain security filter chain.
     *
     * @param http the http
     * @return the security filter chain
     * @throws Exception the exception
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        /**
         * 默认全放开, 由网关直接校验如果没有权限无法调用到服务中来.服务中如果有token可以获取用户,如果没有token直接放开即可
         */
        http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .antMatchers("/**").permitAll())
                .oauth2ResourceServer(
                        oauth2 -> {
                            JvsDefaultBearerTokenResolver defaultBearerTokenResolver = new JvsDefaultBearerTokenResolver();
                            oauth2.opaqueToken(tokenConfigurer -> tokenConfigurer.introspector(token -> {
                                        OAuth2Authorization user = oAuth2AuthorizationService.findByToken(token, OAuth2TokenType.ACCESS_TOKEN);
                                        if (ObjectNull.isNull(user)) {
                                            UserDto userDto = new UserDto();
                                            userDto.setAccountName("anonymous");
                                            CustomUser customUser = new CustomUser();
                                            customUser.setUserDto(userDto);
                                            return customUser;
                                        }
                                        CustomUser attribute = user.getAttribute("user");
                                        SystemThreadLocal.set(USER, attribute);
                                        SystemThreadLocal.set(SysConstant.USERID, attribute.getUserDto().getId());
                                        TenantContextHolder.setTenantId(attribute.getUserDto().getTenantId());
                                        return attribute;
                                    }))
                                    .bearerTokenResolver(defaultBearerTokenResolver);
                        })
                .headers().frameOptions().disable().and().csrf().disable();
        return http.build();
    }

    /**
     * User details service user details service.
     *
     * @return the user details service
     */
    @Bean
    @ConditionalOnMissingBean(UserDetailsService.class)
    public UserDetailsService userDetailsService() {
        return username -> new CustomUser();
    }

    /**
     * Feign请求增强, 用请求头传递用户Token
     * <p>
     * 增强失败时不作处理(无Token时)
     *
     * @return the request interceptor
     */
    @Bean
    public RequestInterceptor oauth2FeignRequestInterceptor() {
        return template -> {
            try {
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (Objects.nonNull(requestAttributes)) {
                    HttpServletRequest request = requestAttributes.getRequest();
                    template.header(HttpHeaders.AUTHORIZATION, request.getHeader(HttpHeaders.AUTHORIZATION));
                }
            } catch (Exception e) {
                log.trace("用户权限增强失败: {}", e.getMessage());
            }
        };
    }

    /**
     * Task executor executor.
     * 使用TTL装饰线程池，自动传递TransmittableThreadLocal数据
     * 相比手动TaskDecorator方案，更轻量、更可靠、更易维护
     *
     * @param properties the properties
     * @return the executor
     */
    @Bean
    @ConditionalOnMissingBean
    public Executor taskExecutor(ThreadPoolProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置线程池核心容量
        executor.setCorePoolSize(properties.getCorePoolSize());
        // 设置线程池最大容量
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        // 设置任务队列长度
        executor.setQueueCapacity(properties.getQueueCapacity());
        // 设置线程超时时间
        executor.setKeepAliveSeconds(properties.getKeepAliveTime());
        // 设置线程名称前缀
        executor.setThreadNamePrefix("jvs-executor-");
        // 设置任务丢弃后的处理策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 仅保留清理逻辑，TTL会自动传递SystemThreadLocal等TransmittableThreadLocal数据
        // 但RequestAttributes和SecurityContext需要手动传递（它们不是TTL）
        executor.setTaskDecorator(runnable -> {
            RequestAttributes context = null;
            Authentication authenticationAuthentication = null;
            try {
                context = RequestContextHolder.currentRequestAttributes();
                SecurityContext authentication = SecurityContextHolder.getContext();
                authenticationAuthentication = authentication.getAuthentication();
            } catch (IllegalStateException e) {
            }
            
            Authentication finalAuthenticationAuthentication = authenticationAuthentication;
            RequestAttributes finalContext = context;
            
            return () -> {
                try {
                    // TTL已自动传递SystemThreadLocal和TenantContextHolder
                    // 只需设置RequestAttributes和SecurityContext
                    if (ObjectNull.isNotNull(finalContext)) {
                        RequestContextHolder.setRequestAttributes(finalContext);
                        if (ObjectNull.isNotNull(finalAuthenticationAuthentication)) {
                            SecurityContextHolder.getContext().setAuthentication(finalAuthenticationAuthentication);
                        }
                    }
                    runnable.run();
                } finally {
                    // 清理ThreadLocal，避免线程池复用时产生脏数据
                    SystemThreadLocal.clear();
                    TenantContextHolder.clear();
                    RequestContextHolder.resetRequestAttributes();
                    SecurityContextHolder.clearContext();
                }
            };
        });
        
        executor.initialize();
        // 使用TTL装饰线程池，自动传递TransmittableThreadLocal
        return TtlExecutors.getTtlExecutor(executor);
    }
}
