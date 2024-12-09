package cn.bctools.auth.entity.enums;

import lombok.Getter;

/**
 * @author zhuxiaokang
 * 授权来源
 */
@Getter
public enum AuthSourceEnum {
    /**
     * SYS：低代码系统
     * DINGTALK_INSIDE：钉钉
     * WECHAT_ENTERPRISE_WEB：企业微信
     * OWN: 客户自有系统（定制）
     * STANDARD_OWN：标准客户自有系统登录（低代码设定的对接三方系统登录方式）
     * LDAP
     */
    SYS("低代码平台"),
    DINGTALK_INSIDE("钉钉"),
    WECHAT_ENTERPRISE_WEB("企业微信"),
    LDAP("ldap");

    String msg;

    AuthSourceEnum(String msg) {
        this.msg = msg;
    }
}
