package cn.bctools.data.factory.source.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 公共的配置
 *
 * @author admin
 */
@Configuration
@ConfigurationProperties(prefix = "data.source.common")
@Data
public class DataSourceCommonConfig {
    /**
     * 数据解析时 每一批次的数量 例如 excel 每读1000条入库
     */
    private int excelReadNumber=10000;

}
