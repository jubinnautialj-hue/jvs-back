package cn.bctools.data.factory;

import cn.bctools.oauth2.annotation.EnableJvsMgrResourceServer;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.xxl.job.core.config.annotation.EnableXxlJobExecutor;
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
@EnableXxlJobExecutor
@EnableDiscoveryClient
@SpringBootApplication(exclude = {MongoAutoConfiguration.class})
@EnableAsync
@EnableJvsMgrResourceServer
public class JvsDataFactoryApplication {

    public static void main(String[] args) {

        SpringApplication.run(JvsDataFactoryApplication.class, args);
        JSON.config(JSONWriter.Feature.LargeObject,true);
    }


}
