package cn.bctools.auth.component.other;

import cn.bctools.auth.entity.LoginLog;
import cn.bctools.auth.service.LoginLogService;
import cn.bctools.common.utils.R;
import cn.bctools.web.utils.IpUtil;
import cn.bctools.web.utils.WebUtils;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author wl
 */
@Slf4j
@Component
@AllArgsConstructor
public class OtherAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Autowired
    LoginLogService loginLogService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        R r = R.failed(((OAuth2AuthenticationException) exception).getError().getErrorCode());
        Map<String, String[]> parameterMap = request.getParameterMap();
        loginLogService.save(new LoginLog()
                .setClientId("client_id")
                .setStatus(false)
                .setOperateTime(LocalDateTime.now())
                .setUserAgent(request.getHeader("User-agent"))
                .setIp(IpUtil.getIpAddr(request))
                .setLoginType(request.getParameter("grant_type"))
                .setRemark(JSONObject.toJSONString(parameterMap))
                .setAccountName(request.getParameter("username")));
        WebUtils.write(r, response);
    }

}
