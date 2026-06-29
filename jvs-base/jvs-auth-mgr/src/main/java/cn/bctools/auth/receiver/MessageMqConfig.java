package cn.bctools.auth.receiver;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhuxiaokang
 * 消息管理-消息队列配置
 */
@Configuration
public class MessageMqConfig {

    public static final String KEY_MSG_SEND_INTERIOR = "msg-send-interior";
    public static final String QUEUE_MSG_SEND_INTERIOR = "queue-" + KEY_MSG_SEND_INTERIOR;
    public static final String EXCHANGE_MSG_SEND = "exchange-msg-send";

    @Bean(EXCHANGE_MSG_SEND)
    public Exchange exchangeMsgSend() {
        return ExchangeBuilder.topicExchange(EXCHANGE_MSG_SEND)
                .durable(true)
                .build();
    }

    @Bean
    public Queue timeLimitQueue() {
        return QueueBuilder.durable(QUEUE_MSG_SEND_INTERIOR).build();
    }
}
