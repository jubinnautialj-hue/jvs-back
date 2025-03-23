package cn.bctools.bi;

import cn.bctools.oauth2.annotation.EnableJvsMgrResourceServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author xiaohui
 */
@RefreshScope
@EnableDiscoveryClient
@SpringBootApplication(exclude = {MongoAutoConfiguration.class})
@EnableAsync
@EnableJvsMgrResourceServer
public class JvsBiApplication {

    public static void main(String[] args) {
        SpringApplication.run(JvsBiApplication.class, args);
    }


}
