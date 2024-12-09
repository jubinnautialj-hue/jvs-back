package cn.bctools.oauth2.config;

import cn.bctools.common.utils.SystemThreadLocal;
import cn.hutool.http.HttpUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrors;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jvs The type Jvs default bearer token resolver.
 */
public class JvsDefaultBearerTokenResolver implements BearerTokenResolver {

    /**
     * 获取 token 的正则
     */
    public static final Pattern AUTHORIZATION_PATTERN = Pattern.compile("^Bearer (?<token>[a-zA-Z0-9-._~+/]+=*)$",
            Pattern.CASE_INSENSITIVE);

    @Override
    public String resolve(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        try {
            String url = request.getHeader("Referer");
            String clientId = HttpUtil.decodeParamMap(url, Charset.defaultCharset()).get("client_id");
            SystemThreadLocal.set("clientId", clientId);
        } catch (Exception e) {
        }
        if (!StringUtils.startsWithIgnoreCase(authorization, "bearer")) {
            return null;
        }
        Matcher matcher = AUTHORIZATION_PATTERN.matcher(authorization);
        if (!matcher.matches()) {
            BearerTokenError error = BearerTokenErrors.invalidToken("Bearer token is malformed");
            throw new OAuth2AuthenticationException(error);
        }
        String authorizationHeaderToken = matcher.group("token");
        if (authorizationHeaderToken != null) {
            return authorizationHeaderToken;
        } else {
            return null;
        }
    }
}
