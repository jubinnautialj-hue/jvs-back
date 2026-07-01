package cn.bctools.design.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author jvs
 * 配置 excel 导出最大限制设置
 * The type Design config.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "design")
public class DesignConfig {

    /**
     * 导出的最大限制
     */
    Integer excelExportMax = 10000;
    /**
     * 发布模板时总数
     */
    Integer templateMaxDataTotal = 5000;
    /**
     * 单个模型可导出的最大数据量
     */
    Integer templateMaxSingleModelDataTotal = 100;
    /**
     * 清理历史日志清洗，默认30天内，超过 30天的不能查询访问
     */
    Integer lastCleanLogTime = 30;
    /**
     * 列表页按钮功能，,默认是兼容的，兼容
     */
    Boolean pageButtonFormulaChinese = true;
    /**
     * 是否启用 ai
     */
    Boolean ai = false;
}
