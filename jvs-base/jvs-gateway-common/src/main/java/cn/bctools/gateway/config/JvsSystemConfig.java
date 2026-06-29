package cn.bctools.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author gx
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jvs")
public class JvsSystemConfig {


    /**
     * 域名,或ip地址.如果是多租户模式,请填写为 主域名,写入cookie 时将写入到此域名中
     */
    public String domain;

    /**
     * 多租户模式,默认为false,
     */
    public Boolean multiTenantMode = false;

}
