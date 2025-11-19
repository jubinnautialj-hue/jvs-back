package cn.bctools.gateway.config;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.entity.dto.UserInfoDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.gateway.dto.CheckToken;
import cn.bctools.oauth2.dto.CustomUser;
import cn.bctools.redis.utils.RedisUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

/**
 * @author Administrator
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class AuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    RedisUtils redisUtils;

    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisSerializer value = RedisSerializer.java();
        StringRedisSerializer key = new StringRedisSerializer();
        RedisSerializationContext.SerializationPair<String> keySerializationPair =
                RedisSerializationContext.SerializationPair.fromSerializer(key);
        RedisSerializationContext.SerializationPair<Object> valueSerializationPair =
                RedisSerializationContext.SerializationPair.fromSerializer(value);
        RedisSerializationContext.SerializationPair<Object> hashValueSerializationPair =
                RedisSerializationContext.SerializationPair.fromSerializer(value);

        RedisSerializationContext<String, Object> context = new RedisSerializationContext<String, Object>() {
            @Override
            public SerializationPair getKeySerializationPair() {
                return keySerializationPair;
            }

            @Override
            public SerializationPair getValueSerializationPair() {
                return valueSerializationPair;
            }

            @Override
            public SerializationPair getHashKeySerializationPair() {
                return keySerializationPair;
            }

            @Override
            public SerializationPair getHashValueSerializationPair() {
                return hashValueSerializationPair;
            }

            @Override
            public SerializationPair<String> getStringSerializationPair() {
                return keySerializationPair;
            }
        };
        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }

    /**
     * token 用户校验规则
     * 1  不用登陆,开放地址,直接放开
     * 2  未匹配资源,如果没有则表示直接登陆即可访问, 不需要资源权限
     * 3  匹配到资源,看用户是否具有权限
     *
     * @param mono
     * @param authorizationContext
     * @return
     */
    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> mono, AuthorizationContext authorizationContext) {
        // 1、从Redis中获取当前路径可访问角色列表
        List<String> list = (List<String>) authorizationContext.getExchange().getAttributes().get(JvsServerBearerTokenAuthenticationConverter.JVS_PERMISSION_LIST);
        String ip = (String) authorizationContext.getExchange().getAttributes().get(JvsServerBearerTokenAuthenticationConverter.JVS_IP);
        String referer = (String) authorizationContext.getExchange().getAttributes().get(JvsServerBearerTokenAuthenticationConverter.REFERER);
        if (ObjectNull.isNull(list)) {
            if (ObjectNull.isNull(ip)) {
                //如果地址没有匹配到,默认放开
                return Mono.just(new AuthorizationDecision(true));
            } else {
                return mono.map(e -> ((CustomUser) e.getPrincipal()))
                        .filter(e -> {
                                    String tenantId = e.getUserDto().getTenantId();
                                    authorizationContext.getExchange().getAttributes().put(SysConstant.TENANTID, tenantId);
                                    Boolean b = SpringContextUtil.getMode() ||
                                                ObjectNull.isNotNull(referer) && e.getUserDto().getUserAgent().startsWith(referer);
                                    return b;
                                }
                        )
                        .switchIfEmpty(Mono.error(new BusinessException("登录已过期").setCode(-2)))
                        .map(e -> new AuthorizationDecision(true))
                        .defaultIfEmpty(new AuthorizationDecision(false));
            }
        }
        return mono
                .map(e -> ((CustomUser) e.getPrincipal()))
                .filter(e -> {
                    return SpringContextUtil.getMode() ||
                           ObjectNull.isNotNull(referer) && e.getUserDto().getUserAgent().startsWith(referer);
                })
                .switchIfEmpty(Mono.error(new BusinessException("登录已过期").setCode(-2)))
                // 2、认证通过且角色匹配的用户可访问当前路径,
                .flatMapIterable(e -> {
                    String tenantId = e.getUserDto().getTenantId();
                    authorizationContext.getExchange().getAttributes().put(SysConstant.TENANTID, tenantId);
                    return e.getPermissions();
                })
                .map(String::valueOf)
                .any((e) -> list.contains(e))
                .map((v) -> new AuthorizationDecision((Boolean) v))
                .defaultIfEmpty(new AuthorizationDecision(false));
    }
}
