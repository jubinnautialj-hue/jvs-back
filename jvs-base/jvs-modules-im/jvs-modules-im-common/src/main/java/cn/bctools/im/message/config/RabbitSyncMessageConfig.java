package cn.bctools.im.message.config;

import cn.bctools.im.message.constant.RabbitMqConstant;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ZhuXiaoKang
 * @Description 消息持久化RabbitMQ配置
 */
@Configuration
public class RabbitSyncMessageConfig {

    @Bean
    public DirectExchange syncMessageExchange() {
        return new DirectExchange(RabbitMqConstant.EXCHANGE_SYNC_MESSAGE);
    }

    @Bean
    public Queue syncChatQueue() {
        return QueueBuilder.durable(RabbitMqConstant.QUEUE_SYNC_CHAT).build();
    }

    @Bean
    public Queue syncNotifyQueue() {
        return QueueBuilder.durable(RabbitMqConstant.QUEUE_SYNC_NOTIFY).build();
    }

    @Bean
    public Binding syncChatBinding() {
        return BindingBuilder
                .bind(syncChatQueue())
                .to(syncMessageExchange())
                .with(RabbitMqConstant.ROUTING_SYNC_CHAT);

    }

    @Bean
    public Binding syncNotifyBinding() {
        return BindingBuilder
                .bind(syncNotifyQueue())
                .to(syncMessageExchange())
                .with(RabbitMqConstant.ROUTING_SYNC_NOTIFY);

    }
}
