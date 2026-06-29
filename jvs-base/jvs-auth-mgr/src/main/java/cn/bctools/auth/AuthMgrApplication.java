package cn.bctools.auth;

import cn.bctools.auth.service.InitConfigService;
import cn.bctools.oauth2.annotation.EnableJvsMgrResourceServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author gj
 * 认证授权中心
 */
@EnableAsync
@Slf4j
@EnableDiscoveryClient
@EnableJvsMgrResourceServer
@SpringBootApplication
public class AuthMgrApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(AuthMgrApplication.class, args);
        //启动刷新配置
        InitConfigService bean = run.getBean(InitConfigService.class);
        bean.refresh();
    }

}
