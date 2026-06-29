package cn.bctools.auth.login.util;

import cn.bctools.auth.entity.enums.SysConfigTypeEnum;
import cn.bctools.auth.service.impl.SysConfigServiceImpl;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigBase;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.SystemThreadLocal;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

/**
 * @author zhuxiaokang
 * 获取登录授权配置
 */
public class AuthConfigUtil {

    /**
     * 授权登录配置
     */
    private static final String AUTH_LOGIN_CONFIG_CACHE = "auth_login_config_cache";
//    /**
//     * 应用id
//     */
//    private static final String CURRENT_APP_ID = "current_app_id";

//    public static void setCurrentAppId(String appId) {
//        SystemThreadLocal.set(CURRENT_APP_ID, appId);
//    }
//
//    public static String getCurrentAppId() {
//        return SystemThreadLocal.get(CURRENT_APP_ID);
//    }

    /**
     * 获取登录授权配置
     *
     * @param type 类型
     * @return
     */
    public static JSONObject getAuthConfig(SysConfigTypeEnum type) {
        JSONObject authConfig = SystemThreadLocal.get(AUTH_LOGIN_CONFIG_CACHE);
        if (ObjectNull.isNotNull(authConfig)) {
            return authConfig;
        }
        SysConfigServiceImpl sysConfigService = SpringContextUtil.getBean(SysConfigServiceImpl.class);
        SysConfigBase config = null;
        switch (type) {
            case WX_ENTERPRISE:
                config = sysConfigService.getConfig(ConfigsTypeEnum.ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION);
                break;
            case DING_H5:
                config = sysConfigService.getConfig(ConfigsTypeEnum.NAIL_APPLICATION_CONFIGURATION);
                break;
            case SMS:
                config = sysConfigService.getConfig(ConfigsTypeEnum.SMS_CONFIGURATION);
                break;
            case LDAP:
                config = sysConfigService.getConfig(ConfigsTypeEnum.LDAP_CONFIGURATION);
                break;
            case WECHAT_MP:
                config = sysConfigService.getConfig(ConfigsTypeEnum.WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }

        if (!config.getEnable()) {
            throw new BusinessException("系统异常", type.getDesc());
        }
        authConfig = JSON.parseObject(JSON.toJSONString(config));
        SystemThreadLocal.set(AUTH_LOGIN_CONFIG_CACHE, authConfig);
        return authConfig;
    }

//    public static String appId(SysConfigTypeEnum type) {
//        return getAuthConfig(type).getString(SysConfigFieldEnum.APP_ID.getValue());
//    }
//
//    public static String secret(SysConfigTypeEnum type) {
//        return getAuthConfig(type).getString(SysConfigFieldEnum.APP_SECRET.getValue());
//    }
//
//    public static String tenantId(SysConfigTypeEnum type) {
//        return getAuthConfig(type).getString(SysConfigFieldEnum.TENANT_ID.getValue());
//    }
//
//    public static String agentId(SysConfigTypeEnum type) {
//        return getAuthConfig(type).getString(SysConfigFieldEnum.AGENT_ID.getValue());
//    }
//
//    public static String redirectUri(SysConfigTypeEnum type) {
//        return getAuthConfig(type).getString(SysConfigFieldEnum.REDIRECT_URI.getValue());
//    }
//
//    public static String aesKey(SysConfigTypeEnum type) {
//        return Optional.ofNullable(getAuthConfig(type).getString(SysConfigFieldEnum.AES_KEY.getValue())).orElse("");
//    }
//
//    public static String token(SysConfigTypeEnum type) {
//        return Optional.ofNullable(getAuthConfig(type).getString(SysConfigFieldEnum.TOKEN.getValue())).orElse("");
//    }
//
//    public static String base(SysConfigTypeEnum type) {
//        return getAuthConfig(type).getString(SysConfigFieldEnum.BASE.getValue());
//    }
//
//    public static String urls(SysConfigTypeEnum type) {
//        return getAuthConfig(type).getString(SysConfigFieldEnum.URLS.getValue());
//    }
//
//    public static String username(SysConfigTypeEnum type) {
//        return getAuthConfig(type).getString(SysConfigFieldEnum.USERNAME.getValue());
//    }
//
//    public static String password(SysConfigTypeEnum type) {
//        return getAuthConfig(type).getString(SysConfigFieldEnum.PASSWORD.getValue());
//    }
//
//    public static String accountAttribute(SysConfigTypeEnum type) {
//        return getAuthConfig(type).getString(SysConfigFieldEnum.ACCOUNT_ATTRIBUTE.getValue());
//    }
//
//    public static String deptIdAttribute(SysConfigTypeEnum type) {
//        return getAuthConfig(type).getString(SysConfigFieldEnum.DEPT_ID_ATTRIBUTE.getValue());
//    }
//
//    public static String userObjectClass(SysConfigTypeEnum type) {
//        return getAuthConfig(type).getString(SysConfigFieldEnum.USER_OBJECT_CLASS.getValue());
//    }
//
//    public static String ouObjectClass(SysConfigTypeEnum type) {
//        return getAuthConfig(type).getString(SysConfigFieldEnum.OU_OBJECT_CLASS.getValue());
//    }
}
