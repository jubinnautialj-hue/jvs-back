package cn.bctools.auth.saas;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author guojing
 * jvs 是否开启saas模式, 如果未开启, 则无法创建租户, 无法注销组织. 用户邀请等功能 都无法正常使用只有 saas多租户模式才能正常使用,默认为不启用
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "saas")
public class SaasConfig {

    /**
     * 默认不启用saas 功能
     */
    boolean enable = false;

}
