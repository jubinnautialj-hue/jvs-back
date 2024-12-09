package cn.bctools.auth.service;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.gateway.entity.Apply;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * @author auto
 */
@Slf4j
@Service
@AllArgsConstructor
public class JvsClientDetailsServiceImpl implements RegisteredClientRepository {

    private final ApplyService applyService;

    @Override
    public void save(RegisteredClient registeredClient) {
        //由mgr处理
    }

    @Override
    public RegisteredClient findById(String id) {
        return null;
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        Apply apply = applyService.loadClientByClientId(clientId);
        if (ObjectNull.isNull(apply)) {
            throw new OAuth2AuthenticationException("终端不存在");
        }
        if (apply.getEnable()) {
            RegisteredClient.Builder builder = RegisteredClient.withId(clientId)
                    .clientId(clientId)
                    .clientSecret(apply.getAppSecret())
                    .clientAuthenticationMethods(clientAuthenticationMethods -> {
                        clientAuthenticationMethods.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
                        clientAuthenticationMethods.add(ClientAuthenticationMethod.CLIENT_SECRET_POST);
                    });
            apply.getAuthorizedGrantTypes().forEach(e -> {
                builder.authorizationGrantType(new AuthorizationGrantType(e));
            });
            return builder.tokenSettings(TokenSettings.builder().accessTokenFormat(OAuth2TokenFormat.REFERENCE)
                    .accessTokenTimeToLive(Duration.ofSeconds(Optional
                            .ofNullable(apply.getAccessTokenValiditySeconds()).orElse(60 * 60 * 24)))
                    .refreshTokenTimeToLive(
                            Duration.ofSeconds(Optional.ofNullable(apply.getRefreshTokenValiditySeconds())
                                    .orElse(60 * 60 * 24 * 30))).build()).build();
        }
        throw new OAuth2AuthenticationException("终端不可用");
    }
}
