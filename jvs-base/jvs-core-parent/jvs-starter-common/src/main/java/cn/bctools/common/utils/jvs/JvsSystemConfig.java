package cn.bctools.common.utils.jvs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author guojing
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jvs")
public class JvsSystemConfig {

    /**
     * 用于登陆时的声明配置信息
     */
    List<Information> filingInformation;

    /**
     * kkfile 的url地址
     */
    String kkfileUrl;

    /**
     * 域名,或ip地址.如果是多租户模式,请填写为 主域名,写入cookie 时将写入到此域名中
     */
    String domain;
    /**
     * 协议  http:// 或 https://
     */
    String protocol = "http://";
    /**
     * ai 的域名或端口号
     */
    String aidomain;

    /**
     * 轻应用的标识域名，如果用户在访问登录时，使用此域名。则直接将其匹配到后，将只返回对应的应用数据
     */
    List<String> identificationDomain;

    /**
     * 多租户模式,默认为false,
     */
    Boolean multiTenantMode = false;

    /**
     * 配置相相互跳转的功能
     */
    List<JvsServiceConfig> service;

    /**
     * 一些特殊的权限标识
     */
    List<String> identifications;

    /**
     * 低代码默认模式（用于控制未切换模式时，默认使用的模式）。可选：GA-正式模式，BETA-测试模式，DEV-开发模式
     */
    public String designDefaultMode = "GA";

    /**
     * 自定义的 http 超时时间
     */
    private int connectTimeout = 30 * 10 * 1000;

    /**
     * 自定义的 socket 超时时间
     */
    private int socketTimeout = 10 * 1000;

}
