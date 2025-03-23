package cn.bctools.data.factory.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * doris链接信息
 *
 * @author xiaohui
 */
@ConfigurationProperties(prefix = "doris")
@Data
public class DorisConfig {
    /**
     * 连接的ip
     */
    private String ip;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 密码
     */
    private String passWord;
    /**
     * FE 上的 http server 端口
     */
    private String httpPort = "8030";
    /**
     * FE 上的 mysql server 端口
     */
    private String queryPort = "9030";
    /**
     * 数据库名称
     */
    private String libraryName;
}
