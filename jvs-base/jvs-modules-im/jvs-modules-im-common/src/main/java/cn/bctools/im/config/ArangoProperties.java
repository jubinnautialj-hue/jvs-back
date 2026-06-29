package cn.bctools.im.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: ZhuXiaoKang
 * @Description: ArangoDB配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "arangodb")
public class ArangoProperties {

    /**
     * 数据库地址
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 数据库名
     */
    private String dbName;
}
