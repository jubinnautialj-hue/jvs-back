package cn.bctools.oauth2.config;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author wl
 */
@Slf4j
@Component
@AllArgsConstructor
public class JvsOAuth2AuthorizationServiceImpl implements OAuth2AuthorizationService {

    RedisUtils redisUtils;
    public static final RedisSerializer<Object> SERIALIZER = RedisSerializer.java();

    /**
     * 用户本地缓存,避免序列化导致的速度降低问题
     */
    static TimedCache<String, OAuth2Authorization> userMap = CacheUtil.newTimedCache(900);

    @Override
    public void save(OAuth2Authorization authorization) {
        OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getAccessToken();
        setToken(accessToken.getToken(), authorization, OAuth2ParameterNames.ACCESS_TOKEN);
        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = authorization.getRefreshToken();
        setToken(refreshToken.getToken(), authorization, OAuth2ParameterNames.REFRESH_TOKEN);
    }

    private void setToken(AbstractOAuth2Token token, OAuth2Authorization authorization, String type) {
        long between = ChronoUnit.SECONDS.between(token.getIssuedAt(), token.getExpiresAt());
        redisUtils.setExpire(type + ":" + token.getTokenValue(), authorization, between, TimeUnit.SECONDS, SERIALIZER);
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getAccessToken();
        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = authorization.getRefreshToken();
        redisUtils.del(OAuth2ParameterNames.ACCESS_TOKEN + ":" + accessToken.getToken().getTokenValue());
        redisUtils.del(OAuth2ParameterNames.REFRESH_TOKEN + ":" + refreshToken.getToken().getTokenValue());
    }

    public void refreshToken(OAuth2Authorization e, OAuth2Authorization authorization) {
        //删除token
        redisUtils.del(OAuth2ParameterNames.ACCESS_TOKEN + ":" + e.getAccessToken().getToken().getTokenValue());
        //将新值放到刷新token中
        setToken(e.getRefreshToken().getToken(), authorization, OAuth2ParameterNames.ACCESS_TOKEN);
    }

    @Override
    @Nullable
    public OAuth2Authorization findById(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        String key = tokenType.getValue() + ":" + token;
        Object o = redisUtils.get(key, RedisSerializer.java());
        if (ObjectNull.isNull(o)) {
            throw new OAuth2AuthenticationException("登录已过期");
        }
        return (OAuth2Authorization) o;
    }

    /**
     * 获取同一个cookie下的用户
     *
     * @param jvs
     * @return
     */
    public List<OAuth2Authorization> keys(String jvs, OAuth2TokenType oAuth2TokenType) {
        Object o = redisUtils.get("jvs:token:" + jvs);
        if(ObjectNull.isNull(o)){
            return new ArrayList<>();
        }
        return Optional.of(o).map( e -> ((List<String>) e))
                .orElseGet(ArrayList::new)
                .stream()
                .filter(e -> e.endsWith(oAuth2TokenType.getValue()))
                .map(e -> e.replaceAll(oAuth2TokenType.getValue(), ""))
                .map(e -> {
                    try {
                        return findByToken(e, oAuth2TokenType);
                    } catch (Exception exception) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
