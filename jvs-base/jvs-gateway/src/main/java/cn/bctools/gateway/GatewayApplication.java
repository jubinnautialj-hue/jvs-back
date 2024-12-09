package cn.bctools.gateway;

import cn.bctools.gateway.filter.GrayReactiveLoadBalancerClientFilter;
import cn.bctools.gray.config.GrayLoadBalancerClientConfiguration;
import cn.bctools.gray.rule.VersionLoadBalancer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.config.GatewayLoadBalancerProperties;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.context.annotation.Bean;

/**
 * 网关应用
 *
 * @author gj
 */
@RefreshScope
@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    @ConditionalOnBean(GrayLoadBalancerClientConfiguration.class)
    public ReactiveLoadBalancerClientFilter gatewayLoadBalancerClientFilter(DiscoveryClient discoveryClient, GatewayLoadBalancerProperties loadBalancerProperties, VersionLoadBalancer versionLoadBalancer) {
        return new GrayReactiveLoadBalancerClientFilter(loadBalancerProperties, discoveryClient, versionLoadBalancer);
    }

}
