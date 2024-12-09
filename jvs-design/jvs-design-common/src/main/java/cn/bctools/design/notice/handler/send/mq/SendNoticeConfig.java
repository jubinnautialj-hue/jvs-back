package cn.bctools.design.notice.handler.send.mq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhuxiaokang
 * 解析配置发送消息
 */
@Configuration
public class SendNoticeConfig {

    public static final String PARSE_NOTICE_SEND_EXCHANGE = "jvs-parseNoticeSend";
    public static final String PARSE_NOTICE_SEND_QUEUE = "jvs-parseNoticeSend-queue";
    public static final String PARSE_NOTICE_SEND_ROUTING = "jvs-parseNoticeSend-routing";

    @Bean
    public TopicExchange parseNoticeSendExchange(){
        Map<String, Object> pros = new HashMap<>(1);
        return new TopicExchange(PARSE_NOTICE_SEND_EXCHANGE, true, false, pros);
    }

    @Bean
    public Queue parseNoticeSendQueue(){
        return QueueBuilder.durable(PARSE_NOTICE_SEND_QUEUE).build();
    }

    @Bean
    public Binding parseNoticeSendBinding(){
        return BindingBuilder.bind(parseNoticeSendQueue()).to(parseNoticeSendExchange()).with(PARSE_NOTICE_SEND_ROUTING);
    }
}
