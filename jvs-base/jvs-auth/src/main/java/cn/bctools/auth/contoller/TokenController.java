package cn.bctools.auth.contoller;

import cn.bctools.auth.component.UserRoleComponent;
import cn.bctools.auth.component.other.OtherOAuth2AccessTokenResponse;
import cn.bctools.auth.entity.OauthOther;
import cn.bctools.auth.mapper.OauthOtherMapper;
import cn.bctools.auth.service.PermissionService;
import cn.bctools.common.utils.*;
import cn.bctools.common.utils.jvs.JvsSystemConfig;
import cn.bctools.auth.service.LoginLogService;
import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysApplyConfig;
import cn.bctools.oauth2.config.JvsOAuth2AuthorizationServiceImpl;
import cn.bctools.oauth2.dto.CustomUser;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.utils.UrlBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Administrator
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/token")
public class TokenController {

    private final RedisUtils redisUtils;
    private final OAuth2AuthorizationService authorizationService;
    private final JvsOAuth2AuthorizationServiceImpl jvsOAuth2AuthorizationService;
    private final JvsSystemConfig informationConfig;
    private final OauthOtherMapper oauthOtherMapper;
    private final LoginLogService loginLogService;
    private final SysConfigsService sysConfigsService;
    private final UserRoleComponent userRoleComponent;
    private final PermissionService permissionService;

    /**
     * 内部跳转时的记录
     */
    @GetMapping("/{clientId}")
    public void inside(@PathVariable("clientId") String clientId, @RequestParam("redirect_url") String redirectUrl, @RequestHeader(value = "Authorization", required = false) String token, HttpServletResponse response) throws IOException {
        log.info("clientId:{} ,token:{} , redirect_url:{}", clientId, token, redirectUrl);
        if (ObjectNull.isNotNull(token)) {
            token = token.replaceAll("Bearer ", "");
            CustomUser attribute = authorizationService.findByToken(token, OAuth2TokenType.ACCESS_TOKEN).getAttribute("user");
            UserDto userDto = attribute.getUserDto();
            //记录一下次数
            if (ObjectNull.isNotNull(clientId)) {
                userDto.setLoginType("inside");
                userDto.setClientId(clientId);
                userDto.setClientName(clientId);
                loginLogService.loginSuccessful(userDto);
            }
        }
        response.sendRedirect(redirectUrl + "?client_id=" + clientId);
    }


    /**
     * 判断cookie 是否有数据
     * 用户登录：当用户成功登录后，服务器会生成一个包含用户信息的Cookie，并将其发送给客户端浏览器。这个Cookie通常包含用户的身份标识符（如用户ID）和其他相关信息（如会话密钥或加密的用户数据）。
     * 客户端存储Cookie：客户端浏览器会将接收到的Cookie存储在本地的内存中，通常会将其保存在浏览器的本地存储（Local Storage）或Cookie中。
     * 客户端验证：在后续的每次请求中，客户端浏览器都会在请求头中包含这个Cookie，将其发送给服务器。服务器通过验证Cookie中的用户信息来判断用户是否登录，以及其身份和权限。
     * 服务器验证：服务器接收到请求时，会从请求头中获取Cookie，并验证其中的用户信息。根据验证结果，服务器会执行相应的操作。如果验证成功，则说明用户已登录，可以执行受保护的敏感操作（如访问个人主页、查看个人信息等）；如果验证失败或Cookie不存在，则说明用户未登录，通常会重定向到登录页面或执行其他未授权的操作。
     * 会话管理：为了确保用户会话的安全性，服务器通常会在每个会话之间使用不同的会话密钥或加密的用户数据来验证用户的身份。如果服务器检测到会话密钥或加密的用户数据无效或过期，则可以采取适当的措施，如强制用户重新登录或删除无效的会话
     *
     * @return
     */
    @GetMapping
    public R index(@RequestParam("client_id") String clientId, HttpServletRequest request, HttpServletResponse response) {
        if (ObjectNull.isNull(request.getCookies())) {
            return R.ok();
        }
        OtherOAuth2AccessTokenResponse accessTokenResponse = Arrays.stream(request.getCookies())
                .filter(e -> e.getName().equals(SysConstant.JVS))
                .map(e -> e.getValue())
                .findFirst()
                //获取用户对象
                .map(e -> SpringContextUtil.getBean(JvsOAuth2AuthorizationServiceImpl.class).keys(e, OAuth2TokenType.ACCESS_TOKEN))
                .filter(ObjectNull::isNotNull)
                .map(e -> e.get(0))
                .map(e -> {
                    CustomUser user = e.getAttribute("user");
                    OtherOAuth2AccessTokenResponse tokenResponse = new OtherOAuth2AccessTokenResponse();
                    tokenResponse.setUsername(e.getPrincipalName());
                    UserDto userDto = user.getUserDto();
                    ConfigsTypeEnum configsTypeEnum = Arrays.stream(ConfigsTypeEnum.values()).filter(s -> clientId.equals(s.clientId)).findAny().get();
                    String tenantId = userDto.getTenantId();
                    TenantContextHolder.setTenantId(tenantId);
                    SysApplyConfig config = sysConfigsService.getConfig(configsTypeEnum);
                    userDto.getTenant().setSystemName(ObjectNull.isNotNull(config.getSystemName()) ? config.getSystemName() : configsTypeEnum.serviceName).setLogo(config.getLogo()).setIcon(config.getIcon());
                    //此处需要替换这个终端的Logo和icon
                    tokenResponse.setUserDto(PasswordUtil.encodePassword(JSON.toJSONString(userDto)));
                    List<String> roleIds = userRoleComponent.getUserRoleIds(user.getUserDto().getId());
                    List<String> permission = permissionService.getPermission(user.getUserDto().getPlatformAdmin(), user.getUserDto().getAdminFlag(), user.getUserDto().getTenantId(), roleIds);
                    //这里看是否需要刷新权限标识
                    tokenResponse.setPermissions(permission);
                    tokenResponse.setAccess_token(e.getAccessToken().getToken().getTokenValue());
                    tokenResponse.setRefresh_token(e.getRefreshToken().getToken().getTokenValue());
                    return tokenResponse;
                })
                .orElseGet(() -> null);
        if (ObjectNull.isNull(accessTokenResponse)) {
            //清空 cookie
            Cookie cookie = new Cookie(SysConstant.JVS, "");
            cookie.setMaxAge(0);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            String domain = informationConfig.getDomain();
            if (ObjectNull.isNotNull(domain)) {
                cookie.setDomain(domain);
            }
            response.addCookie(cookie);
        }
        return R.ok(accessTokenResponse);
    }

    /**
     * 三方系统退出token
     *
     * @param type  三方平台类型
     * @param token 三方系统的token
     */
    @GetMapping("/logout/{type}/{token}")
    public R logoutToken(@PathVariable String token, @PathVariable String type, HttpServletResponse response) {
        //根据三方平台的token调用退出
        Object o = redisUtils.get("justToken:" + type + token);
        if (ObjectNull.isNotNull(o)) {
            return logout(o.toString(), response);
        }
        return R.ok();
    }

    /**
     * @param authHeader
     * @return
     */
    @GetMapping("/logout")
    public R logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader, HttpServletResponse response) {
        Cookie cookie = new Cookie(SysConstant.JVS, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        String domain = informationConfig.getDomain();
        if (ObjectNull.isNotNull(domain)) {
            cookie.setDomain(domain);
        }
        response.addCookie(cookie);
        //根据这个用户退出其它同级终端
        if (StrUtil.isNotBlank(authHeader)) {
            try {
                OAuth2Authorization authorization = authorizationService.findByToken(authHeader.replaceAll("Bearer ", ""), OAuth2TokenType.ACCESS_TOKEN);
                CustomUser user = authorization.getAttribute("user");
                //判断是否是三方登录如果是调用一下三方登录的退出
                String loginType = user.getUserDto().getLoginType();
                OauthOther oauthOther = oauthOtherMapper.selectOne(Wrappers.query(new OauthOther().setType(loginType)));
                //查询所有对应的token
                jvsOAuth2AuthorizationService.keys(user.getJvs(), OAuth2TokenType.ACCESS_TOKEN).forEach(authorizationService::remove);
                //删除Jvs cookie
                redisUtils.del("jvs:token:" + user.getUserDto().getId() + user.getJvs());
                if (ObjectNull.isNotNull(oauthOther)) {
                    if (ObjectNull.isNotNull(oauthOther.getLogoutUri())) {
                        Object o = JvsJsonPath.read(user.getUserDto().getExceptions(), "token.accessToken");
                        //调用退出功能
                        try {
                            String s = HttpUtil.get(UrlBuilder.fromBaseUrl(oauthOther.getLogoutUri()).queryParam("access_token", o).build());
                            log.info("退出" + loginType + "返回内容为" + s);
                        } catch (Exception e) {

                        }
                    }
                }
            } catch (Exception e) {
                log.error("用户已退出");
            }
        }
        return R.ok("退出成功");
    }

}
