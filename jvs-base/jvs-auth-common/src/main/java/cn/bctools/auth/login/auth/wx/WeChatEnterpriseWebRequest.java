package cn.bctools.auth.login.auth.wx;

import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigEnterriseWeChat;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.config.AuthDefaultSource;
import me.zhyd.oauth.config.AuthSource;
import me.zhyd.oauth.enums.scope.AuthWeChatEnterpriseWebScope;
import me.zhyd.oauth.request.AbstractAuthWeChatEnterpriseRequest;
import me.zhyd.oauth.utils.AuthScopeUtils;
import me.zhyd.oauth.utils.UrlBuilder;

/**
 * <p>
 * 企业微信网页登录
 * </p>
 *
 * @author liguanhua (347826496(a)qq.com)
 * @since 1.15.9
 */
public class WeChatEnterpriseWebRequest extends AbstractAuthWeChatEnterpriseRequest implements AuthSource {
    public WeChatEnterpriseWebRequest(AuthConfig config) {
        super(config, AuthDefaultSource.WECHAT_ENTERPRISE_WEB);
    }

    public WeChatEnterpriseWebRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthDefaultSource.WECHAT_ENTERPRISE_WEB, authStateCache);
    }

    @Override
    public String authorize(String state) {
        return UrlBuilder.fromBaseUrl(authorize())
                .queryParam("appid", config.getClientId())
                .queryParam("redirect_uri", config.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", this.getScopes(",", false, AuthScopeUtils.getDefaultScopes(AuthWeChatEnterpriseWebScope.values())))
                .queryParam("state", getRealState(state).concat("#wechat_redirect"))
                .build();
    }

    @Override
    public String authorize() {
        SysConfigsService bean = SpringContextUtil.getBean(SysConfigsService.class);
        SysConfigEnterriseWeChat config = bean.getConfig(ConfigsTypeEnum.ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION);
        if (config.getEnable()) {
            String openUrl = config.getOpenUrl();
            return openUrl + "/connect/oauth2/authorize";
        }
        return AuthDefaultSource.WECHAT_ENTERPRISE_WEB.authorize();
    }

    @Override
    public String accessToken() {
        SysConfigsService bean = SpringContextUtil.getBean(SysConfigsService.class);
        SysConfigEnterriseWeChat config = bean.getConfig(ConfigsTypeEnum.ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION);
        if (config.getEnable()) {
            String baseApiUrl = config.getBaseApiUrl();
            return baseApiUrl + "/cgi-bin/gettoken";
        }
        return AuthDefaultSource.WECHAT_ENTERPRISE_WEB.accessToken();
    }

    @Override
    public String userInfo() {
        SysConfigsService bean = SpringContextUtil.getBean(SysConfigsService.class);
        SysConfigEnterriseWeChat config = bean.getConfig(ConfigsTypeEnum.ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION);
        if (config.getEnable()) {
            String baseApiUrl = config.getBaseApiUrl();
            return baseApiUrl + "/cgi-bin/user/getuserinfo";
        }
        return AuthDefaultSource.WECHAT_ENTERPRISE_WEB.userInfo();
    }

}
