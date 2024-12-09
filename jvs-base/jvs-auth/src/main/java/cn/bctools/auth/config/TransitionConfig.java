package cn.bctools.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * @author guojing
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ip")
public class TransitionConfig {

    /**
     * 是否开启IP转换功能
     */
    Boolean transition = true;

}
