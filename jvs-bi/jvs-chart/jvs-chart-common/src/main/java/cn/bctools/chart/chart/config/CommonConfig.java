package cn.bctools.chart.chart.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 公共的配置
 *
 * @author admin
 */
@Configuration
@ConfigurationProperties(prefix = "common")
@Data
public class CommonConfig {
    /**
     * minio 内网ip地址
     */
    private String s3Ip;
}
