package cn.bctools.ai.config;

import feign.Client;
import org.springframework.context.annotation.Bean;

/**
 * @author jvs
 */
public class DisableLoadBalancerConfig {
    @Bean
    public Client feignClient() {
        // 使用默认HTTP客户端，禁用负载均衡
        return new Client.Default(null, null);
    }
}
