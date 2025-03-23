package cn.bctools.screen;

import cn.bctools.data.factory.config.DorisBeanInit;
import cn.bctools.oauth2.annotation.EnableJvsMgrResourceServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Indexed;

@RefreshScope
@EnableDiscoveryClient
@SpringBootApplication
@EnableAsync
@EnableJvsMgrResourceServer
@Indexed
@Import(DorisBeanInit.class)
public class JvsScreenApplication {

    public static void main(String[] args) {
        SpringApplication.run(JvsScreenApplication.class, args);
    }
}
