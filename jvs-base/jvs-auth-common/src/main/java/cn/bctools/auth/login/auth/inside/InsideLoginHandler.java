package cn.bctools.auth.login.auth.inside;

import cn.bctools.auth.entity.User;
import cn.bctools.auth.login.LoginHandler;
import cn.bctools.auth.service.UserService;
import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.entity.dto.UserInfoDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.oauth2.config.JvsOAuth2AuthorizationServiceImpl;
import cn.bctools.web.utils.IpUtil;
import cn.bctools.web.utils.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * The type Inside login handler.
 *
 * @author guojing
 */
@Slf4j
@Component("inside")
public class InsideLoginHandler implements LoginHandler<InsideDto> {

    /**
     * The User service.
     */
    @Autowired
    UserService userService;

    /**
     * The Agent.
     */
    static final String agent = "User-agent";

    @Override
    public Boolean encryption() {
        return true;
    }

    @Override
    public User handle(String code, String appId, InsideDto insideDto) {
        log.info("内部登陆请求参数为:{}", code);
        String accessToken = insideDto.getToken();
        log.info("accessToken is :{}", accessToken);
        //判断cookie
        HttpServletRequest request = WebUtils.getRequest();
        String header = request.getHeader(agent);
        String ipAddr = IpUtil.getIpAddr(request);
        //判断是否是移动端,移动端,判断token ,pc端判断cookie
        boolean mobile = IpUtil.isMobile();
        UserInfoDto userInfoDto = Arrays.stream(request.getCookies())
                .filter(e -> e.getName().equals(SysConstant.JVS))
                .map(e -> e.getValue())
                .findFirst()
                //获取用户对象
                .map(e -> SpringContextUtil.getBean(JvsOAuth2AuthorizationServiceImpl.class).keys(e, OAuth2TokenType.ACCESS_TOKEN))
                .map(e -> e.get(0).getAttribute("user"))
                .map(e -> (UserInfoDto) e)
                //匹配Ip地址
                .filter(e -> e.getUserDto().getIp().equals(ipAddr))
                .orElseThrow(() -> new BusinessException("Jump_登录失败"));
        if (mobile) {
            return userService.getById(userInfoDto.getUserDto().getId());
        } else {
            if (userInfoDto.getUserDto().getUserAgent().equals(header)) {
                log.error("浏览器标识不一致,认定为不是同一个浏览器,{},{}", userInfoDto.getUserDto().getUserAgent(), header);
                return userService.getById(userInfoDto.getUserDto().getId());
            }
        }
        throw new BusinessException("Jump_登录失败");
    }

}
