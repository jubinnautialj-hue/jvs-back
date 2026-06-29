package cn.bctools.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhuxiaokang
 * 三方登录配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jvs.other-login")
public class OtherLoginConfig {

    /**
     * true-开启三方平台新用户登录自动注册；false-不开启自动注册(默认)
     */
    private Boolean enableAutomaticRegister = Boolean.FALSE;
}
