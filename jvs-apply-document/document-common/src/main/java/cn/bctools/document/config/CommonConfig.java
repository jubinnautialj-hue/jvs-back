package cn.bctools.document.config;

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
     * onlyOffice url
     */
    private String onlyOfficeUrl;
    /**
     * onlyOffice url
     */
    private String ocrUrl;
    /**
     * 打包时的文件存储逻辑
     */
    private String downPath = "C:\\Users\\wl\\Desktop\\biDownFile\\";
    /**
     * 解压路径
     * 注意不能在打包路径下面
     */
    private String unzipPath = "C:\\Users\\wl\\Desktop\\unzip\\";
    /**
     * 导入与导出的zip 密码
     */
    private String zipPwd = "jvs123";

}
