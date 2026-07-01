package cn.bctools.design.rule.swagger.listener;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jvs
 * 刷新swagger api缓存MQ配置（广播）
 */
@Configuration
public class RefreshSwaggerApiMqConfig {

    public static final String EXCHANGE = "jvs-refreshSwaggerRuleApi";
    public static final String QUEUE = "jvs-refreshSwaggerRuleApi-queue";

    @Bean
    public FanoutExchange refreshSwaggerRuleApiExchange() {
        Map<String, Object> pros = new HashMap<>(1);
        return new FanoutExchange(EXCHANGE, true, false, pros);
    }

    @Bean
    public Queue refreshSwaggerRuleApiQueue(){
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding refreshSwaggerRuleApiBinding(){
        return BindingBuilder.bind(refreshSwaggerRuleApiQueue()).to(refreshSwaggerRuleApiExchange());
    }

}
