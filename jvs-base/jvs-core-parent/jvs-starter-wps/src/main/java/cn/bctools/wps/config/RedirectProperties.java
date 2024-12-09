package cn.bctools.wps.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 */
@Data
@Component
@ConfigurationProperties(prefix = "redirect")
public class RedirectProperties {

    private String key;
    private String value;

}
