package cn.bctools.feign.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SwaggerProperties
 *
 * @author My_gj
 */
@Data
@ConfigurationProperties("swagger")
public class SwaggerProperties {
    /**
     * 标题
     **/
    private String title = "Swagger未配置，请自行配置";
    /**
     * 详细描述
     */
    private String description = "Swagger未配置，请自行配置";

    /**
     * 自定义OpenApi组名
     */
    private String openApiGroupName = "openApi";
    /**
     * 是否启用swagger文档(默认关闭)
     **/
    private Boolean enable = false;

}
