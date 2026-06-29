
package cn.bctools.oss.template.local;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author jvs 本地文件
 */
@Data
@ConfigurationProperties(prefix = "local")
public class LocalFileProperties {

    /**
     * 是否开启
     */
    private boolean enable;

    /**
     * 默认路径
     */
    private String basePath;

}
