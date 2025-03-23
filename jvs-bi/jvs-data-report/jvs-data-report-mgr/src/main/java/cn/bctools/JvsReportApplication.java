package cn.bctools;

import cn.bctools.oauth2.annotation.EnableJvsMgrResourceServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@RefreshScope
@EnableDiscoveryClient
@SpringBootApplication
@EnableAsync
@EnableJvsMgrResourceServer
//@Import(DorisBeanInit.class)
@EnableFeignClients
public class JvsReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(JvsReportApplication.class, args);
    }

}
