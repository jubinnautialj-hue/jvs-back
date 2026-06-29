package cn.bctools.auth.component.other;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.util.List;

/**
 * 为保持和原有结构一致
 *
 * @author wl
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtherOAuth2AccessTokenResponse {
    OAuth2AccessToken.TokenType tokenType;
    String access_token;
    long expires_in;
    String refresh_token;
    List<String> permissions;
    String userDto;
    String username;

}
