package cn.bctools.rule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 逻辑引擎扩展的示例demo服务
 *
 * @@author jvs
 */
@EnableDiscoveryClient
@SpringBootApplication
public class DataBaseApplication {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(DataBaseApplication.class, args);
    }

}
