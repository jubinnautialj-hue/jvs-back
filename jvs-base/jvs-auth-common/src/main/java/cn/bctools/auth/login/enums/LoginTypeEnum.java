package cn.bctools.auth.login.enums;

import cn.bctools.auth.entity.enums.SysConfigTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 登录授权类型
 */
@Getter
@AllArgsConstructor
public enum LoginTypeEnum {

    /**
     * value：配置类型值
     * desc：描述
     * configType：登录授权配置类型
     */
    DINGTALK_INSIDE("DINGTALK_INSIDE", "钉钉客户端", SysConfigTypeEnum.DING_H5),
    DINGTALK_SCAN("DINGTALK_SCAN", "钉钉扫码", SysConfigTypeEnum.DING_H5),
    WECHAT_ENTERPRISE_WEB("WECHAT_ENTERPRISE_WEB", "企业微信", SysConfigTypeEnum.WX_ENTERPRISE),
    WECHAT_ENTERPRISE("WECHAT_ENTERPRISE", "企业微信扫码", SysConfigTypeEnum.WX_ENTERPRISE),
    WECHAT_MP("WECHAT_MP", "微信公众号", SysConfigTypeEnum.WECHAT_MP),
    WECHAT_OPEN("WECHAT_OPEN", "微信公众号", SysConfigTypeEnum.WECHAT_MP),
    LDAP("LDAP", "LDAP", SysConfigTypeEnum.LDAP),
    OWN("OWN", "客户自有系统登录（定制登录）", null),
    STANDARD_OWN("STANDARD_OWN", "标准客户自有系统登录（低代码设定的对接三方系统登录方式）", null),
    ;

    private final String value;
    private final String desc;
    private final SysConfigTypeEnum configType;

    public static LoginTypeEnum getType(String value) {
        for (LoginTypeEnum typeEnum : LoginTypeEnum.values()) {
            if (value.equals(typeEnum.getValue())) {
                return typeEnum;
            }
        }
        return null;
    }
}
