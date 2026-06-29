package cn.bctools.auth.component.other;

import cn.bctools.common.utils.jvs.JvsSystemConfig;
import cn.bctools.auth.service.LoginLogService;
import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.PasswordUtil;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.oauth2.dto.CustomUser;
import cn.bctools.oauth2.dto.OtherAuthenticationToken;
import cn.bctools.web.utils.WebUtils;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.temporal.ChronoUnit;

/**
 * @author wl
 */
@Slf4j
@Component
public class OtherAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Resource
    JvsSystemConfig informationConfig;

    @Resource
    LoginLogService loginLogService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AccessTokenAuthenticationToken accessTokenAuthentication = (OAuth2AccessTokenAuthenticationToken) authentication;


        CustomUser principal = (CustomUser) ((OtherAuthenticationToken) authentication.getPrincipal()).getPrincipal();
        //记录线程,用于用户清除上一次的缓存数据,所以刷新token也会触发用户相关信息的刷新
        SystemThreadLocal.set(SysConstant.USERID, principal.getUserDto().getId());
        //记录日志
        UserDto userDto = principal.getUserDto();
        //只存登陆日志,刷新日志不存
        if (ObjectNull.isNull(WebUtils.getRequest().getParameter("refresh_token"))) {
            loginLogService.loginSuccessful(userDto);
        }
        OAuth2AccessToken accessToken = accessTokenAuthentication.getAccessToken();

        long between = ChronoUnit.SECONDS.between(accessToken.getIssuedAt(), accessToken.getExpiresAt());
        //清空调不需要返回的数据
        principal.getUserDto()
                .setCancelFlag(null)
                .setClientId(null)
                .setClientName(null)
                .setIp(null)
                .setPassword(null)
                .setUserAgent(null);

        OtherOAuth2AccessTokenResponse accessTokenResponse = OtherOAuth2AccessTokenResponse.builder()
                .access_token(accessTokenAuthentication.getAccessToken().getTokenValue())
                .refresh_token(accessTokenAuthentication.getRefreshToken().getTokenValue())
                .expires_in(between)
                .username(principal.getUsername())
                .permissions(principal.getPermissions())
                .tokenType(accessTokenAuthentication.getAccessToken().getTokenType())
                .userDto(PasswordUtil.encodePassword(JSON.toJSONString(principal.getUserDto())))
                .build();
        //用户多业务系统相互跳转
        Cookie cookie = new Cookie(SysConstant.JVS, principal.getJvs());
        cookie.setPath("/");
        cookie.setMaxAge(30*24*60*60);
        cookie.setHttpOnly(false);
        String domain = informationConfig.getDomain();
        if (ObjectNull.isNotNull(domain)) {
            cookie.setDomain(domain);
        }
        response.addCookie(cookie);
        new FastJsonHttpMessageConverter().write(accessTokenResponse, MediaType.APPLICATION_JSON, new ServletServerHttpResponse(response));
    }

}
