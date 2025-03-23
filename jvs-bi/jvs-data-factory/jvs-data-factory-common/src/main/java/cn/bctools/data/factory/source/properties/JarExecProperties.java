package cn.bctools.data.factory.source.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class JarExecProperties {

    @Value("${jar.exec.connectionTimeout:5000}")
    private Integer connectionTimeout;

    @Value("${jar.exec.readTimeout:15000}")
    private Integer readTimeout;
}
