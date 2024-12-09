package cn.bctools.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 三方登录类型
 */
@Getter
@AllArgsConstructor
public enum OtherLoginTypeEnum {
    /**
     * 钉钉登陆
     */
    Ding("钉钉"),
    /**
     * ldap
     */
    LDAP("LDAP"),
    /**
     * 企业微信
     */
    WX_ENTERPRISE("企业微信"),
    /**
     * 微信公众号
     */
    WECHAT_MP("微信公众号"),
    /**
     * 微信开放平台
     */
    WECHAT_OPEN("微信开放平台"),
    /**
     * 微信小程序
     */
    wxapp("微信小程序"),
    /**
     * 客户自有系统登录（定制登录）
     */
    OWN("定制登录"),
    /**
     * 标准客户自有系统登录（低代码设定的对接三方系统登录方式）
     */
    STANDARD_OWN("标准登录");

    private String desc;
}
