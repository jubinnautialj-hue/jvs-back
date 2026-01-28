package cn.bctools.auth.component.other;

import cn.bctools.auth.entity.LoginRules;
import cn.bctools.auth.mapper.LoginRulesMapper;
import cn.bctools.auth.service.UserDetailsServiceImpl;
import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.entity.dto.TenantsDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.*;
import cn.bctools.common.utils.jvs.JvsSystemConfig;
import cn.bctools.gateway.entity.SysConfigs;
import cn.bctools.oauth2.config.JvsOAuth2AuthorizationServiceImpl;
import cn.bctools.oauth2.dto.CustomUser;
import cn.bctools.oauth2.dto.OtherAuthenticationToken;
import cn.bctools.redis.utils.RedisUtils;
import cn.bctools.web.utils.IpUtil;
import cn.bctools.web.utils.WebUtils;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Date;

/**
 * @author glg
 */
@Slf4j
public class OtherAuthenticationProvider extends AccountStatusUserDetailsChecker implements AuthenticationProvider {
    private final RegisteredClientRepository registeredClientRepository;
    private final JvsOAuth2AuthorizationServiceImpl oAuth2AuthorizationService;
    private final UserDetailsServiceImpl userDetailsService;
    private final RedisUtils redisUtils;
    private final OAuth2TokenGenerator oAuth2TokenGenerator = new OtherOAuth2TokenGenerator();
    private final OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
    private final PasswordEncoder passwordEncoder;
    private final LoginRulesMapper loginRulesMapper;


    public OtherAuthenticationProvider(OAuth2AuthorizationService oAuth2AuthorizationService, UserDetailsServiceImpl userDetailsService, RegisteredClientRepository registeredClientRepository, RedisUtils redisUtils, PasswordEncoder passwordEncoder, LoginRulesMapper loginRulesMapper) {
        this.oAuth2AuthorizationService = (JvsOAuth2AuthorizationServiceImpl) oAuth2AuthorizationService;
        this.userDetailsService = userDetailsService;
        this.registeredClientRepository = registeredClientRepository;
        this.redisUtils = redisUtils;
        this.passwordEncoder = passwordEncoder;
        this.loginRulesMapper = loginRulesMapper;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OtherAuthenticationToken token = (OtherAuthenticationToken) authentication;
        //判断有没有帐号密码
        String password = token.getPassword();
        String userName = token.getUserName();
        String clientId = token.getClientId();
        String otherParameter = token.getOtherParameter();
        //根据clientId获取
        RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);

        OAuth2RefreshToken refreshToken = null;
        String ipAddr = IpUtil.getIpAddr(WebUtils.getRequest());
        //判断是否是禁止登录
        List<LoginRules> loginRulesLogs = loginRulesMapper.selectList(new LambdaQueryWrapper<LoginRules>().eq(LoginRules::getStatus, true).eq(LoginRules::getIp, ipAddr));
        if (ObjectNull.isNotNull(loginRulesLogs)) {
            //根据时间判断当前是否是禁止登录
            loginRulesLogs.forEach(e -> {
                //获取当前时间，并根据 e.getTime() 判断当前是否是禁止登录,e.getTime 是 [12:00:00,14:00:00]
                Date now = DateUtil.date();
                Date startTime = DateUtil.parse(e.getTime().get(0));
                Date endTime = DateUtil.parse(e.getTime().get(1));
                if (DateUtil.compare(now, startTime) >= 0 && DateUtil.compare(now, endTime) <= 0) {
                    throw new AccountExpiredException(SpringContextUtil.msg("当前时间禁止登录"));
                }
            });
        }

        CustomUser userDetails = null;
        if (ObjectNull.isNotNull(password, userName, clientId)) {
            //给帐号密码添加错误校验次数
            checkLockUser(userName + ":" + ipAddr);
            userDetails = (CustomUser) userDetailsService.loadUserByUsername(userName);
            //检查用户状态
            check(userDetails);
            //校验密码
            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                //添加次数
                int i = loginTimeIncrease(userName + ":" + ipAddr);
                if (i <= 0) {
                    checkLockUser(userName + ":" + ipAddr);
                }
                String message = "用户名或密码错误," + i + "次后将锁定";
                throw new AccountExpiredException(SpringContextUtil.msg(message));
            } else {
                //输对了删除记录
                redisUtils.del(SysConstant.redisKey("checklogin", userName + ":" + ipAddr));
                redisUtils.del(SysConstant.redisKey("lock", userName + ":" + ipAddr));
            }
            token.setPrincipal(userDetails);
        } else if (ObjectNull.isNotNull(otherParameter)) {
            userDetails = (CustomUser) userDetailsService.loadUserByOtherAuth(otherParameter, clientId);
            //检查用户状态
            check(userDetails);
        } else if (ObjectNull.isNotNull(token.getRefreshToken(), clientId)) {
            //刷新校验token
            OAuth2Authorization oAuth2Authorization = null;
            try {
                oAuth2Authorization = oAuth2AuthorizationService.findByToken(token.getRefreshToken(), OAuth2TokenType.REFRESH_TOKEN);
            } catch (Exception e) {
                throw new AccountExpiredException("登录已过期");
            }
            refreshToken = oAuth2Authorization.getRefreshToken().getToken();
            userDetails = oAuth2Authorization.getAttribute("user");
            //需要去判断用户浏览器是否是更新过，如果更新过就不返回了
            UserDto userDto = userDetails.getUserDto();
            if (userDto.getUserAgent().equals(token.getUserAgent())) {
                SysConfigs sysConfigs = switchTenantConfig(userDto.getId());
                //如果不是切换租户,直接返回当前用户,如果是切换租户,将重新获取用户信息
                if (ObjectNull.isNotNull(sysConfigs.getTenantId())) {
                    List<TenantsDto> tenants = userDto.getTenants();
                    Map<String, TenantsDto> dtoMap = tenants.stream().collect(Collectors.toMap(TenantsDto::getTenantId, Function.identity()));
                    userDetails = userDetailsService.getCustomUser(userDetails.getUserDto().getId(), userDto, sysConfigs, dtoMap);
                }
            } else {
                //清空 cookie
                Cookie cookie = new Cookie(SysConstant.JVS, "");
                cookie.setMaxAge(0);
                cookie.setPath("/");
                cookie.setHttpOnly(true);
                String domain = SpringContextUtil.getBean(JvsSystemConfig.class).getDomain();
                if (ObjectNull.isNotNull(domain)) {
                    cookie.setDomain(domain);
                }
                WebUtils.getResponse().addCookie(cookie);
                throw new AccountExpiredException("刷新失效", new BusinessException("刷新失效", -2));
            }
        } else {
            throw new AccountExpiredException("登录失效", new BusinessException("登录失效", -2));
        }
        //生成唯一标识写入cookie, 通过租户Id进行拼接
        userDetails.setJvs(userDetails.getUserDto().getTenantId() + DigestUtils.md5Hex(userDetails.getUserDto().getId() + userDetails.getUserDto().getIp() + userDetails.getUserDto().getUserAgent()));
        token.setPrincipal(userDetails);
        //设置客户终端
        userDetails.getUserDto().setClientId(registeredClient.getClientId()).setClientName(registeredClient.getClientName());
        //根据用户创建token
        DefaultOAuth2TokenContext.Builder builder = DefaultOAuth2TokenContext.builder().registeredClient(registeredClient).principal(authentication).authorizationServerContext(AuthorizationServerContextHolder.getContext()).authorizedScopes(registeredClient.getScopes()).authorizationGrantType(AuthorizationGrantType.PASSWORD);
        OAuth2Token generate = oAuth2TokenGenerator.generate(builder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build());
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, generate.getTokenValue(), generate.getIssuedAt(), generate.getExpiresAt(), new HashSet<>());
        RegisteredClient build = RegisteredClient.withId(registeredClient.getId()).clientId(registeredClient.getClientId()).authorizationGrantType(AuthorizationGrantType.PASSWORD).build();

        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient).authorizationGrantType(AuthorizationGrantType.PASSWORD).authorizedScopes(registeredClient.getScopes());

        if (refreshToken == null) {
            refreshToken = refreshTokenGenerator.generate(builder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build());
        }

        authorizationBuilder.accessToken(accessToken).refreshToken(refreshToken);
        redisUtils.set("tenant:" + userDetails.getUserDto().getId(), userDetails.getUserDto().getTenantId());

        authorizationBuilder.attribute("user", BeanCopyUtil.copy(userDetails, CustomUser.class));
        authorizationBuilder.principalName(userDetails.getUsername());
        OAuth2Authorization auth2Authorization = authorizationBuilder.build();

        //将同用户同环境下的token存放在一起,方便退出操作
        redisUtils.set("jvs:token:" + userDetails.getJvs(), Arrays.asList(accessToken.getTokenValue() + OAuth2TokenType.ACCESS_TOKEN.getValue(), refreshToken.getTokenValue() + OAuth2TokenType.REFRESH_TOKEN.getValue()), accessToken.getExpiresAt().getEpochSecond());

        if (ObjectNull.isNotNull(token.getRefreshToken())) {
            //如果是刷新token,则将其它为jvs密的token都给删除掉,保留刷新token  前端刷新token时,还是使用的上一次的租户,需要使用新租户
            oAuth2AuthorizationService.keys(userDetails.getJvs(), OAuth2TokenType.ACCESS_TOKEN).forEach(e -> {
                oAuth2AuthorizationService.refreshToken(e, auth2Authorization);
            });
        }
        if (ObjectNull.isNotNull(otherParameter)) {
            if (otherParameter.contains(StringPool.AT)) {
                String[] split = otherParameter.split(StringPool.AT);
                redisUtils.set("justToken:" + split[0] + JvsJsonPath.read(PasswordUtil.decodedPassword(split[1], clientId), "code"), auth2Authorization.getAccessToken().getToken().getTokenValue(), accessToken.getExpiresAt().getEpochSecond());
            }
        }
        oAuth2AuthorizationService.save(auth2Authorization);
        userDetails.getUserDto().setCh(token.getCh());
        return new OAuth2AccessTokenAuthenticationToken(build, authentication, accessToken, refreshToken);
    }

    /**
     * @param userName
     * @return
     */
    private int loginTimeIncrease(String userName) {
        //如果还未过期则直接返回错误
        String key = SysConstant.redisKey("checklogin", userName);
        Object o = redisUtils.get(key);
        if (ObjectNull.isNotNull(o)) {
            //记录次数. 和判断是否锁定
            int value = ((Integer) o) + 1;
            redisUtils.set(key, value, Duration.ofDays(1));
            //如果超过5次,即添加锁定功能
            if (value >= 5) {
                //每错一次+5分钟。
                int i = (value - 5) * 5 + 5;
                redisUtils.set(SysConstant.redisKey("lock", userName), i, Duration.ofMinutes(i));
            }
            return 5 - (Integer) o;
        } else {
            //一天内有效
            redisUtils.set(key, 1, Duration.ofDays(1));
        }
        return 5;
    }

    /**
     * 校验用户是否被限制爆破, 避免密码被泄漏
     *
     * @param userName
     */
    private void checkLockUser(String userName) {
        //如果不为空,判断次数是否大余5次,直接返回失败
        String key = SysConstant.redisKey("lock", userName);
        Object o = redisUtils.get(key);
        if (ObjectNull.isNotNull(o)) {
            throw new AccountExpiredException(SpringContextUtil.msg("分钟后再试", o));
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(OtherAuthenticationToken.class);
    }


    /**
     * 根据请求数据,获取这次的租户id
     *
     * @param userId
     * @return
     */
    public SysConfigs switchTenantConfig(String userId) {
        SysConfigs sysConfigs = new SysConfigs();
        HttpServletRequest request = WebUtils.getRequest();
        String tenantId = request.getParameter("switch");
        if (ObjectNull.isNull(tenantId)) {
            tenantId = (String) redisUtils.get("tenant" + userId);
        }
        if (ObjectNull.isNull(tenantId)) {
            tenantId = "1";
        }
        TenantContextHolder.setTenantId(tenantId);
        //获取终端
        String clientId = request.getParameter("client_id");
        return sysConfigs.setTenantId(tenantId).setClientId(clientId);
    }

}
