package cn.bctools.auth.component.other;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.oauth2.dto.OtherAuthenticationToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;

import javax.servlet.http.HttpServletRequest;

/**
 * @author wl
 */
public class Oauth2OtherAuthenticationConverter implements AuthenticationConverter {

    /**
     * 其它所有的登录
     */
    public static final String LOGIN_OTHER_AUTH_PARAMETER = "login_other_auth_parameter";
    public static final String LOGIN_CH = "ch";

    /**
     * 根据请求返回不同的抽象Token
     *
     * @param request
     * @return
     */
    private AbstractAuthenticationToken getAbstractAuthenticationToken(HttpServletRequest request) {
        String clientId = request.getParameter("client_id");
        String parameter = request.getParameter(LOGIN_OTHER_AUTH_PARAMETER);
        String ch = request.getParameter(LOGIN_CH);
        String username = request.getParameter(OAuth2ParameterNames.USERNAME);
        String password = request.getParameter(OAuth2ParameterNames.PASSWORD);
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        String refreshToken = request.getParameter(OAuth2ParameterNames.REFRESH_TOKEN);
        String userAgent = request.getHeader("User-Agent");
        String tenantId = request.getParameter(SysConstant.TENANTID);
        String loginURL = request.getParameter("loginURL");
        if (ObjectNull.isNotNull(clientId)) {
            return new OtherAuthenticationToken(parameter, ch, clientId, username, password, grantType, refreshToken, tenantId, userAgent,loginURL);
        }
        return null;
    }

    @Override
    public Authentication convert(HttpServletRequest request) {
        return getAbstractAuthenticationToken(request);
    }

}
