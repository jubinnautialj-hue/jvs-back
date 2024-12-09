package cn.bctools.wps.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 */
@Data
@Component
@ConfigurationProperties(prefix = "wps")
public class WpsProperties {

    private String domain;
    private String appid;
    private String appsecret;
    private String downloadHost;
    private String localDir;

}