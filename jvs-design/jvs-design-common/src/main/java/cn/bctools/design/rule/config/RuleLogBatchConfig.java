package cn.bctools.design.rule.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 规则日志批量保存配置
 * 
 * @author assistant
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rule.log")
public class RuleLogBatchConfig {

    /**
     * 是否启用批量保存
     * 默认: true
     */
    private Boolean batchEnabled = true;

    /**
     * 队列容量
     * 默认: 10000
     */
    private Integer queueCapacity = 10000;

    /**
     * 批量保存大小
     * 默认: 100
     */
    private Integer batchSize = 100;

    /**
     * 批量保存间隔（毫秒）
     * 默认: 5000ms (5秒)
     */
    private Long batchInterval = 5000L;
}
