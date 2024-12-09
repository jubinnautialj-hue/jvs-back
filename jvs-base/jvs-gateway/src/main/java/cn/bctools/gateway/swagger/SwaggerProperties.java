package cn.bctools.gateway.swagger;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author guojing
 */
@Data
@Configuration
@ConfigurationProperties("swagger")
public class SwaggerProperties {

    /**
     * 是否启用swagger文档(默认关闭)
     **/
    private Boolean enable = false;

    /**
     * 忽略的服务
     */
    private List<String> ignore = new ArrayList(){{
        add("im");
        add("jvs-auth");
        add("jvs-modules-im-mgr");
    }};
}
