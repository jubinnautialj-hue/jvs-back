package cn.bctools.auth;

import cn.bctools.feign.annotation.EnableJvsFeignClients;
import cn.bctools.oauth2.annotation.EnableJvsMgrResourceServer;
import cn.bctools.oauth2.config.ClientFeignConfig;
import cn.bctools.oauth2.config.JvsAdapter;
import cn.bctools.oauth2.config.JvsOAuth2AuthorizationServiceImpl;
import cn.bctools.oauth2.utils.AuthorityManagementUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * @author gj
 * 认证授权中心
 */
@EnableJvsFeignClients
@EnableDiscoveryClient
@EnableWebSecurity
@SpringBootApplication
public class AuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }

}
