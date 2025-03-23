package cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.cofig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * seaTunnel 插件的api 配置
 * 方便后续更改
 *
 * @author Administrator
 */
@Configuration
@ConfigurationProperties(prefix = "sea.tunnel.api")
@Data
public class SeaTunnelApiConfig {

    /**
     * 提交任务的 seaTunnel api地址
     */
    private String submitJobUrl = "/hazelcast/rest/maps/submit-job";
    /**
     * 查询单个任务的api地址
     * %s 路径参数 是任务的id
     */
    private String getJobInfoUrl = "/hazelcast/rest/maps/job-info/%s";
    /**
     * 停止作业
     */
    private String stopJobUrl = "/hazelcast/rest/maps/stop-job";

}
