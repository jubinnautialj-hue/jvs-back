package cn.bctools.auth.login;

import cn.bctools.auth.login.enums.LoginTypeEnum;
import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigEnterriseWeChat;
import cn.bctools.common.enums.SysConfigWx;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.web.utils.WebUtils;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.xkcoding.http.config.HttpConfig;
import com.xkcoding.justauth.autoconfigure.ExtendProperties;
import com.xkcoding.justauth.autoconfigure.JustAuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.config.AuthDefaultSource;
import me.zhyd.oauth.config.AuthSource;
import me.zhyd.oauth.enums.AuthResponseStatus;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.request.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author zhuxiaokang
 * 自定义JustAuth的AuthRequestFactory
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthRequestCustomFactory {
    private final AuthStateCache authStateCache;
    private final SysConfigsService sysConfigsService;

    /**
     * 返回AuthRequest对象
     *
     * @param source {@link AuthSource}
     * @return {@link AuthRequest}
     */
    public AuthRequest get(String source) {
        if (StrUtil.isBlank(source)) {
            throw new AuthException(AuthResponseStatus.NO_AUTH_SOURCE);
        }
        // 获取 JustAuth 中已存在的
        AuthRequest authRequest = getDefaultRequest(source);

        if (authRequest == null) {
            throw new AuthException(AuthResponseStatus.UNSUPPORTED);
        }

        return authRequest;
    }

    private AuthRequest getDefaultRequest(String oauthType) {

        JustAuthProperties properties = new JustAuthProperties();
        AuthConfig authConfig = new AuthConfig();
        //根据不同类型返回不同结果
        switch (LoginTypeEnum.valueOf(oauthType)) {
            case WECHAT_OPEN:
            case WECHAT_MP:
                SysConfigWx wxconfig = sysConfigsService.getConfig(ConfigsTypeEnum.WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION);
                if (wxconfig.getEnable()) {
                    authConfig.setClientId(wxconfig.getAppKey());
                    authConfig.setClientSecret(wxconfig.getAppSecret());
                }
                oauthType = LoginTypeEnum.WECHAT_MP.name();
                break;
            case LDAP:
                //todo
                break;

            case DINGTALK_SCAN:
                //todo
                break;

            case DINGTALK_INSIDE:
                //todo
                break;
            case WECHAT_ENTERPRISE:

            case WECHAT_ENTERPRISE_WEB:
                SysConfigEnterriseWeChat enwxconfig = sysConfigsService.getConfig(ConfigsTypeEnum.ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION);
                if (enwxconfig.getEnable()) {
                    authConfig.setAgentId(enwxconfig.getAgentId().toString());
                    authConfig.setClientSecret(enwxconfig.getAppSecret());
                    authConfig.setClientId(enwxconfig.getAppId());
                    authConfig.setRedirectUri(convertRedirectUri(enwxconfig.getRedirectUri()));
                    authConfig.setIgnoreCheckState(Boolean.TRUE);
                }
                break;
            default:
                throw new BusinessException("系统异常");
        }

        if (LoginTypeEnum.WECHAT_ENTERPRISE.getValue().equals(oauthType)) {
            authConfig.setIgnoreCheckState(Boolean.TRUE);
        }

        properties.getType().put(oauthType, authConfig);
        return getDefaultRequest(oauthType, properties);
    }

    /**
     * 封装重定向地址
     *
     * @param redirectUri 重定向地址相对路径
     * @return 重定向地址绝对路径
     */
    private String convertRedirectUri(String redirectUri) {
        // 拼接重定向地址
        if (ObjectNull.isNotNull(redirectUri)) {
            HttpServletRequest request = WebUtils.getRequest();
            String url = Optional.ofNullable(request.getHeader("Referer")).orElseThrow(() -> new BusinessException("获取域名失败"));
            URL u = URLUtil.url(url);
            String host = URLUtil.getHost(u).getHost();
            StringBuilder rdUri = new StringBuilder(u.getProtocol()).append("://").append(host);
            if (u.getPort() > 0) {
                rdUri.append(":").append(u.getPort());
            }
            rdUri.append(redirectUri);
            redirectUri = rdUri.toString();
        }
        return redirectUri;
    }

    /**
     * 获取自定义的 request
     *
     * @param properties 配置
     * @param source     {@link AuthSource}
     * @return {@link AuthRequest}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private AuthRequest getExtendRequest(JustAuthProperties properties, String source) {
        String upperSource = source.toUpperCase();
        try {
            Class clazz = properties.getExtend().getEnumClass();
            EnumUtil.fromString(clazz, upperSource);
        } catch (IllegalArgumentException e) {
            // 无自定义匹配
            return null;
        }

        Map<String, ExtendProperties.ExtendRequestConfig> extendConfig = properties.getExtend().getConfig();

        // key 转大写
        Map<String, ExtendProperties.ExtendRequestConfig> upperConfig = new HashMap<>(6);
        extendConfig.forEach((k, v) -> upperConfig.put(k.toUpperCase(), v));

        ExtendProperties.ExtendRequestConfig extendRequestConfig = upperConfig.get(upperSource);
        if (extendRequestConfig != null) {

            // 配置 http config
            configureHttpConfig(upperSource, extendRequestConfig, properties.getHttpConfig());

            Class<? extends AuthRequest> requestClass = extendRequestConfig.getRequestClass();

            if (requestClass != null) {
                // 反射获取 Request 对象，所以必须实现 2 个参数的构造方法
                return ReflectUtil.newInstance(requestClass, (AuthConfig) extendRequestConfig, authStateCache);
            }
        }

        return null;
    }


    /**
     * 获取默认的 Request
     *
     * @param source {@link AuthSource}
     * @return {@link AuthRequest}
     */
    private AuthRequest getDefaultRequest(String source, JustAuthProperties properties) {
        AuthDefaultSource authDefaultSource;

        try {
            authDefaultSource = EnumUtil.fromString(AuthDefaultSource.class, source.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 无自定义匹配
            return null;
        }
        AuthConfig config = properties.getType().get(authDefaultSource.name());
        // 找不到对应关系，直接返回空
        if (config == null) {
            return null;
        }

        // 配置 http config
        configureHttpConfig(authDefaultSource.name(), config, properties.getHttpConfig());

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
            case WECHAT_OPEN:
                return new AuthWeChatOpenRequest(config, authStateCache);
            case WECHAT_MP:
                return new AuthWeChatMpRequest(config, authStateCache);
            case WECHAT_ENTERPRISE:
                return new AuthWeChatEnterpriseQrcodeRequest(config, authStateCache);
            case WECHAT_ENTERPRISE_WEB:
                return new AuthWeChatEnterpriseWebRequest(config, authStateCache);
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
                return null;
        }
    }

    /**
     * 配置 http 相关的配置
     *
     * @param authSource {@link AuthSource}
     * @param authConfig {@link AuthConfig}
     */
    private void configureHttpConfig(String authSource, AuthConfig authConfig, JustAuthProperties.JustAuthHttpConfig httpConfig) {
        if (null == httpConfig) {
            return;
        }
        Map<String, JustAuthProperties.JustAuthProxyConfig> proxyConfigMap = httpConfig.getProxy();
        if (CollectionUtils.isEmpty(proxyConfigMap)) {
            return;
        }
        JustAuthProperties.JustAuthProxyConfig proxyConfig = proxyConfigMap.get(authSource);

        if (null == proxyConfig) {
            return;
        }

        authConfig.setHttpConfig(HttpConfig.builder()
                .timeout(httpConfig.getTimeout())
                .proxy(new Proxy(Proxy.Type.valueOf(proxyConfig.getType()), new InetSocketAddress(proxyConfig.getHostname(), proxyConfig.getPort())))
                .build());
    }
}
