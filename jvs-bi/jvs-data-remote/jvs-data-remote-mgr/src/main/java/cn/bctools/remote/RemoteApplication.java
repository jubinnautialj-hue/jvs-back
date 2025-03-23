package cn.bctools.remote;

import cn.bctools.oauth2.annotation.EnableJvsMgrResourceServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@RefreshScope
@EnableDiscoveryClient
@SpringBootApplication
@EnableAsync
@EnableJvsMgrResourceServer
@EnableScheduling
public class RemoteApplication {

    public static void main(String[] args) {
        SpringApplication.run(RemoteApplication.class, args);
    }
}
