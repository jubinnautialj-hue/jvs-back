package cn.bctools.database.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author guojing
 */
@ConfigurationProperties("sql")
@Configuration
@Data
@Primary
@RefreshScope
public class SqlProperties {

    /**
     * 动态创建租户数据库,租户id动态路由开关
     */
    private boolean dynamicTenantDatabase = false;

}
