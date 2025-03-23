package cn.bctools.auth.login.auth.wx;

import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigEnterriseWeChat;
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
 * 企业微信二维码登录
 * </p>
 *
 * @author yangkai.shen (https://xkcoding.com)
 * @author liguanhua (347826496(a)qq.com) 重构该类，将通用方法提取
 * @author lyadong.zhang (yadong.zhang0415(a)gmail.com) 修改类名
 * @since 1.10.0
 */
public class WeChatEnterpriseQrcodeRequest extends AbstractAuthWeChatEnterpriseRequest implements AuthSource {
    public WeChatEnterpriseQrcodeRequest(AuthConfig config) {
        super(config, AuthDefaultSource.WECHAT_ENTERPRISE);
    }

    public WeChatEnterpriseQrcodeRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthDefaultSource.WECHAT_ENTERPRISE, authStateCache);
    }

    @Override
    public String authorize(String state) {
        return UrlBuilder.fromBaseUrl(authorize())
                .queryParam("appid", config.getClientId())
                .queryParam("agentid", config.getAgentId())
                .queryParam("redirect_uri", config.getRedirectUri())
                .queryParam("state", getRealState(state))
                .build();
    }

    @Override
    public String authorize() {
        SysConfigsService bean = SpringContextUtil.getBean(SysConfigsService.class);
        SysConfigEnterriseWeChat config = bean.getConfig(ConfigsTypeEnum.ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION);
        if (config.getEnable()) {
            String scanUrl = config.getScanUrl();
            return scanUrl + "/wwopen/sso/qrConnect";
        }
        return AuthDefaultSource.WECHAT_ENTERPRISE.authorize();
    }

    @Override
    public String accessToken() {
        SysConfigsService bean = SpringContextUtil.getBean(SysConfigsService.class);
        SysConfigEnterriseWeChat config = bean.getConfig(ConfigsTypeEnum.ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION);
        if (config.getEnable()) {
            String baseApiUrl = config.getBaseApiUrl();
            return baseApiUrl + "/cgi-bin/gettoken";
        }
        return AuthDefaultSource.WECHAT_ENTERPRISE.accessToken();
    }

    @Override
    public String userInfo() {
        SysConfigsService bean = SpringContextUtil.getBean(SysConfigsService.class);
        SysConfigEnterriseWeChat config = bean.getConfig(ConfigsTypeEnum.ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION);
        if (config.getEnable()) {
            String baseApiUrl = config.getBaseApiUrl();
            return baseApiUrl + "/cgi-bin/user/getuserinfo";
        }
        return AuthDefaultSource.WECHAT_ENTERPRISE.userInfo();
    }
}
