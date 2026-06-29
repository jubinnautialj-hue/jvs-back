package cn.bctools.gateway.config;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.gateway.dto.CheckToken;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import reactor.core.publisher.Mono;

/**
 * @author guojing
 */
@Slf4j
@AllArgsConstructor
public class JvsReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    OAuth2AuthorizationService oAuth2AuthorizationService;
    ReactiveRedisTemplate<String, Object> redisTemplate;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.justOrEmpty(authentication)
                .filter(a -> a instanceof BearerTokenAuthenticationToken)
                .cast(BearerTokenAuthenticationToken.class)
                .map(BearerTokenAuthenticationToken::getToken)
                .flatMap((accessToken -> redisTemplate.opsForValue().get("jvs:" + OAuth2TokenType.ACCESS_TOKEN.getValue() + ":" + accessToken)
                        .map(e -> (OAuth2Authorization) e)
                        .map(e -> new CheckToken(e.getAttribute("user")))
                        .switchIfEmpty(Mono.error(new BusinessException("登录已过期").setCode(-2)))));
    }

}
