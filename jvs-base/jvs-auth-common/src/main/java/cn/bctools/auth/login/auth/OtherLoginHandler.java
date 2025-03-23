package cn.bctools.auth.login.auth;

import cn.bctools.auth.component.OtherLoginUserInfoComponent;
import cn.bctools.auth.component.cons.OtherUserDto;
import cn.bctools.auth.entity.Dept;
import cn.bctools.auth.entity.OauthOther;
import cn.bctools.auth.entity.User;
import cn.bctools.auth.entity.UserExtension;
import cn.bctools.auth.login.AuthRequestCustomFactory;
import cn.bctools.auth.login.auth.other.OauthOtherRequest;
import cn.bctools.auth.login.auth.other.OtherAuthUser;
import cn.bctools.auth.login.auth.wx.WeChatEnterpriseQrcodeRequest;
import cn.bctools.auth.login.auth.wx.WeChatEnterpriseWebRequest;
import cn.bctools.auth.login.auth.wx.enterprise.BaseWxEnterprise;
import cn.bctools.auth.login.dto.StandardOwnDto;
import cn.bctools.auth.login.dto.SyncUserDto;
import cn.bctools.auth.mapper.OauthOtherMapper;
import cn.bctools.auth.service.UserExtensionService;
import cn.bctools.auth.service.UserService;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.*;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.util.EnumUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xkcoding.justauth.autoconfigure.JustAuthProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.config.AuthDefaultSource;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author guojing
 */
@Slf4j
@Component
@AllArgsConstructor
public class OtherLoginHandler extends BaseWxEnterprise {

    OauthOtherMapper oauthOtherMapper;
    UserService userService;
    AuthRequestCustomFactory factory;
    JustAuthProperties justAuthProperties;
    UserExtensionService userExtensionService;
    AuthStateCache authStateCache;
    RedisUtils redisUtils;
    OtherLoginUserInfoComponent otherLoginUserInfoComponent;

    public User handle(String code, String appId, String type) {
        String[] split = type.split("__");
        String type1 = split[0];
        AuthDefaultRequest authRequest = getAuthDefaultRequest(type1);


        //现在只支持一个即可，不支持其它的
        if ("token".equals(type) && authRequest instanceof OauthOtherRequest) {
            OauthOtherRequest oauthOtherRequest = (OauthOtherRequest) authRequest;
            AuthToken authToken = new AuthToken();
            type1 = oauthOtherRequest.getOauthOther().getName();
            code = JSON.parseObject(PasswordUtil.decodedPassword(code, appId)).getString("token");
            authToken.setAccessToken(code);
            OtherAuthUser authUser = (OtherAuthUser) oauthOtherRequest.getUserInfo(authToken);
            OtherUserDto otherUserDto =
                    new OtherUserDto().setOpenId(authUser.getUuid()).setUserName(authUser.getNickname()).setAccountName(authUser.getAccount()).setEmail(authUser.getEmail()).setLoginType(type1).setAvatar(authUser.getAvatar()).setDeptId(authUser.getCompany()).setOtherUser(authUser.getRawUserInfo());
            log.info("三方用户信息返回后数据组装: {}", JSON.toJSONString(otherUserDto));
            return otherLoginUserInfoComponent.getUser(otherUserDto);
        } else {
            code = JSON.parseObject(PasswordUtil.decodedPassword(code, appId)).getString("code");

            AuthCallback callback = new AuthCallback();
            callback.setCode(code);
            callback.setState(type);
            AuthResponse response = null;
            try {
                response = authRequest.login(callback);
            } catch (Exception e) {
                throw new BusinessException(type + "登录失败" + e.getMessage());
            }
            log.info("登陆其它项目获取用户信息根据格式组装后结果: {}", JSON.toJSONString(response));
            if (!response.ok()) {
                log.error(type + "登录失败: {}", response.getMsg());
                throw new BusinessException(type + "登录失败,三方数据返回为:" + JSONObject.toJSONString(response));
            }
            AuthUser authUser = (AuthUser) response.getData();
            OtherUserDto otherUserDto = new OtherUserDto().setOpenId(authUser.getUuid()).setUserName(authUser.getNickname()).setAccountName(authUser.getUsername()).setEmail(authUser.getEmail()).setLoginType(type1).setAvatar(authUser.getAvatar()).setDeptId(authUser.getCompany()).setOtherUser(authUser.getRawUserInfo());
            if (authUser instanceof OtherAuthUser) {
                otherUserDto.setAccountName(((OtherAuthUser) authUser).getAccount());
            }
            log.info("三方用户信息返回后数据组装: {}", JSON.toJSONString(otherUserDto));
            return otherLoginUserInfoComponent.getUser(otherUserDto);
        }
    }

    public AuthDefaultRequest getAuthDefaultRequest(String type) {
        //todo 判断是否是企业应用,或公众号内部登陆,不是扫码登陆,
        OauthOther oauthOther = oauthOtherMapper.selectOne(Wrappers.query(new OauthOther().setType(type)));
        if (ObjectNull.isNull(oauthOther)) {
            throw new BusinessException("平台未配置此三方快捷登陆");
        }
        AuthConfig config = BeanCopyUtil.copy(oauthOther, AuthConfig.class);
        config.setIgnoreCheckState(true);
        config.setIgnoreCheckRedirectUri(true);
        return requestJust(type, config, oauthOther);
    }

    public AuthDefaultRequest getAuthDefaultRequest(String type, OauthOther oauthOther) {
        if (ObjectNull.isNull(oauthOther)) {
            throw new BusinessException("平台未配置此三方快捷登陆");
        }
        AuthConfig config = BeanCopyUtil.copy(oauthOther, AuthConfig.class);
        config.setIgnoreCheckState(true);
        config.setIgnoreCheckRedirectUri(true);
        return requestJust(type, config, oauthOther);
    }

    public SyncUserDto getDeptAll(String type) {
        //动态类型
        OauthOther one = oauthOtherMapper.selectOne(Wrappers.query(new OauthOther().setType(type)));
        AuthDefaultRequest authRequest = getAuthDefaultRequest(type, one);
        //获取三方的 token
        if (authRequest instanceof OauthOtherRequest) {
            OauthOtherRequest oauthOtherRequest = (OauthOtherRequest) authRequest;
            String accessToken = oauthOtherRequest.getAccessToken();
            List<? extends Dept> remoteDepts = oauthOtherRequest.getDept(accessToken);
            Map<String, Dept> deptMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(remoteDepts)) {
                deptMap = remoteDepts.stream().collect(Collectors.toMap(Dept::getId, Function.identity()));
            }
            SyncUserDto syncUserDto = new SyncUserDto();
            oauthOtherRequest.getUserList(deptMap, syncUserDto, accessToken);
            syncUserDto.setList(remoteDepts);
            return syncUserDto;
        }
        throw new BusinessException("不支持此三方同步");

    }

    /**
     * 根据类型匹配三方登陆
     *
     * @param source
     * @param config
     * @param oauthOther
     * @return
     */
    public AuthDefaultRequest requestJust(String source, AuthConfig config, OauthOther oauthOther) {
        AuthDefaultSource authDefaultSource = null;

        try {
            authDefaultSource = EnumUtil.fromString(AuthDefaultSource.class, source.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 无自定义匹配
            return new OauthOtherRequest(oauthOther, config, authStateCache);
        }

        switch (authDefaultSource) {
            case GITHUB:
                return new AuthGithubRequest(config, authStateCache);
            case WEIBO:
                return new AuthWeiboRequest(config, authStateCache);
            case GITEE:
                return new AuthGiteeRequest(config, authStateCache);
            case DINGTALK:
                return new AuthDingTalkRequest(config, authStateCache);
            case DINGTALK_ACCOUNT:
                return new AuthDingTalkAccountRequest(config, authStateCache);
            case BAIDU:
                return new AuthBaiduRequest(config, authStateCache);
            case CODING:
                return new AuthCodingRequest(config, authStateCache);
            case OSCHINA:
                return new AuthOschinaRequest(config, authStateCache);
            case ALIPAY:
                return new AuthAlipayRequest(config, authStateCache);
            case QQ:
                return new AuthQqRequest(config, authStateCache);
            case WECHAT_OPEN:
                return new AuthWeChatOpenRequest(config, authStateCache);
            case WECHAT_MP:
                return new AuthWeChatMpRequest(config, authStateCache);
            case WECHAT_ENTERPRISE:
                return new WeChatEnterpriseQrcodeRequest(config, authStateCache);
            case WECHAT_ENTERPRISE_WEB:
                return new WeChatEnterpriseWebRequest(config, authStateCache);
            case TAOBAO:
                return new AuthTaobaoRequest(config, authStateCache);
            case GOOGLE:
                return new AuthGoogleRequest(config, authStateCache);
            case FACEBOOK:
                return new AuthFacebookRequest(config, authStateCache);
            case DOUYIN:
                return new AuthDouyinRequest(config, authStateCache);
            case LINKEDIN:
                return new AuthLinkedinRequest(config, authStateCache);
            case MICROSOFT:
                return new AuthMicrosoftRequest(config, authStateCache);
            case MI:
                return new AuthMiRequest(config, authStateCache);
            case TOUTIAO:
                return new AuthToutiaoRequest(config, authStateCache);
            case TEAMBITION:
                return new AuthTeambitionRequest(config, authStateCache);
            case RENREN:
                return new AuthRenrenRequest(config, authStateCache);
            case PINTEREST:
                return new AuthPinterestRequest(config, authStateCache);
            case STACK_OVERFLOW:
                return new AuthStackOverflowRequest(config, authStateCache);
            case HUAWEI:
                return new AuthHuaweiRequest(config, authStateCache);
            case GITLAB:
                return new AuthGitlabRequest(config, authStateCache);
            case KUJIALE:
                return new AuthKujialeRequest(config, authStateCache);
            case ELEME:
                return new AuthElemeRequest(config, authStateCache);
            case MEITUAN:
                return new AuthMeituanRequest(config, authStateCache);
            case TWITTER:
                return new AuthTwitterRequest(config, authStateCache);
            case FEISHU:
                return new AuthFeishuRequest(config, authStateCache);
            case JD:
                return new AuthJdRequest(config, authStateCache);
            case ALIYUN:
                return new AuthAliyunRequest(config, authStateCache);
            case XMLY:
                return new AuthXmlyRequest(config, authStateCache);
            case AMAZON:
                return new AuthAmazonRequest(config, authStateCache);
            case SLACK:
                return new AuthSlackRequest(config, authStateCache);
            case LINE:
                return new AuthLineRequest(config, authStateCache);
            case OKTA:
                return new AuthOktaRequest(config, authStateCache);
            default:
                return new OauthOtherRequest(oauthOther, config, authStateCache);
        }
    }

    /**
     * 绑定三方平台
     *
     * @param user  用户对象
     * @param code  code码
     * @param type  类型
     * @param appId 应用
     */
    public void bind(User user, String code, String type, String appId) {
        String decodedPassword = PasswordUtil.decodedPassword(code, appId);
        StandardOwnDto dto = JSONObject.parseObject(decodedPassword, StandardOwnDto.class);
        AuthCallback callback = new AuthCallback();
        callback.setCode(dto.getCode());
        callback.setState(type);
        AuthDefaultRequest authRequest = getAuthDefaultRequest(type);
        AuthResponse response = authRequest.login(callback);
        log.info("登陆其它项目获取用户信息根据格式组装后结果: {}", JSON.toJSONString(response));
        if (!response.ok()) {
            log.error(type + "绑定失败: {}", response.getMsg());
            throw new BusinessException(type + "绑定失败");
        }
        AuthUser authUser = (AuthUser) response.getData();
        OtherUserDto otherUser = new OtherUserDto().setOpenId(authUser.getUuid()).setUserName(authUser.getNickname()).setAccountName(authUser.getUsername()).setEmail(authUser.getEmail()).setLoginType(type).setAvatar(authUser.getAvatar()).setDeptId(authUser.getCompany()).setOtherUser(authUser.getRawUserInfo());
        log.info("三方用户信息返回后数据组装: {}", JSON.toJSONString(otherUser));
        if (StringUtils.isBlank(otherUser.getLoginType()) || StringUtils.isBlank(otherUser.getOpenId())) {
            throw new BusinessException("绑定失败", "openId or loginType error");
        }
        // 判断是否重复绑定
        UserExtension extension = userExtensionService.getOne(Wrappers.<UserExtension>lambdaQuery().eq(UserExtension::getType, otherUser.getLoginType()).eq(UserExtension::getOpenId, otherUser.getOpenId()));
        if (ObjectNull.isNotNull(extension)) {
            throw new BusinessException("帐号已经绑定,请先解绑");
        }
        // 绑定用户关键信息
        extension = new UserExtension().setUserId(user.getId()).setOpenId(otherUser.getOpenId()).setNickname(otherUser.getUserName()).setType(otherUser.getLoginType()).setExtension(otherUser.getOtherUser());
        userService.updateById(user);
        userExtensionService.save(extension);
    }
}
