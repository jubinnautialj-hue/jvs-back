package cn.bctools.data.factory.config;


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
     * 每次同步开启的并发数
     */
    private String dataxChannel = "5";
    /**
     * 是否开启debug模式 开启debug模式表示不删除中间表 用于排查数据错误问题 谨慎开启
     */
    private Boolean debugStatus;

    /**
     * 生成的datax同步数据json所在的路径前缀 为什么不直接放到资源里面 是方便查看生成后的json 用于排查错误
     * 这里直接置顶datax 所在目录即可
     */
    private String dataxPath;
    /**
     * data jvm配置
     */
    private String dataxJvm;

    /**
     * doris 所有表的副本数量  官方建议3个
     * 自定义是为了防止用户 没有这么多集群  但是 建议至少4台  这样才能发挥doris 架构的性能
     */
    private Integer replicationNum = 3;

    /**
     * SeaTunnel 部署的地址 ip和端口
     */
    private String seaTunnelIpPort;
}
