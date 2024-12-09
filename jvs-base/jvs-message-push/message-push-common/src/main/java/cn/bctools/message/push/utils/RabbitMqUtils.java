package cn.bctools.message.push.utils;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * rabbitMq工具
 * @author xh
 */
@Lazy(false)
@Configuration
public class RabbitMqUtils {

    /**
     * 交换机名称
     */
    public static final String EXCHANGE_TOPICS_INFORM="exchange_topics_inform";
    /**
     * 队列名称 邮件
     */
    public static final String QUEUE_INFORM_EMAIL = "queue_inform_email";
    /**
     * 队列名称 短信
     */
    public static final String QUEUE_INFORM_SMS = "queue_inform_sms";
    /**
     * 队列名称 微信公众号
     */
    public static final String QUEUE_INFORM_WECHAT = "queue_inform_wechat";
    /**
     * 队列名称 站内信
     */
    public static final String QUEUE_INFORM_INSIDE = "queue_inform_inside";
    /**
     * 队列名称 钉钉文本消息
     */
    public static final String QUEUE_INFORM_DING_H5 = "queue_inform_dingding_h5";

    /**
     * 声明交换机
     * @return
     */
    @Bean(EXCHANGE_TOPICS_INFORM)
    @Lazy(false)
    public Exchange EXCHANGE_TOPICS_INFORM(){
        //durable(true) 持久化，mq重启之后交换机还在
        return ExchangeBuilder.directExchange(EXCHANGE_TOPICS_INFORM).durable(true).build();
    }

    /**
     * 声明QUEUE_INFORM_EMAIL队列
     * @return
     */
    @Lazy(false)
    @Bean(QUEUE_INFORM_EMAIL)
    public Queue QUEUE_INFORM_EMAIL(){
        return new Queue(QUEUE_INFORM_EMAIL);
    }

    /**
     * 声明短信队列
     * @return
     */
    @Lazy(false)
    @Bean(QUEUE_INFORM_SMS)
    public Queue QUEUE_INFORM_SMS(){
        return new Queue(QUEUE_INFORM_SMS);
    }

    /**
     * 声明微信公众号队列
     * @return
     */
    @Lazy(false)
    @Bean(QUEUE_INFORM_WECHAT)
    public Queue QUEUE_INFORM_WECHAT(){
        return new Queue(QUEUE_INFORM_WECHAT);
    }

    /**
     * 声明站内信队列
     * @return
     */
    @Lazy(false)
    @Bean(QUEUE_INFORM_INSIDE)
    public Queue QUEUE_INFORM_INSIDE(){
        return new Queue(QUEUE_INFORM_INSIDE);
    }

    /**
     * 声明站内信队列
     * @return
     */
    @Lazy(false)
    @Bean(QUEUE_INFORM_DING_H5)
    public Queue QUEUE_INFORM_DING_H5(){
        return new Queue(QUEUE_INFORM_DING_H5);
    }

    /**
     * 邮件队列绑定交换机，指定routingKey
     * @param queue
     * @param exchange
     * @return
     */
    @Lazy(false)
    @Bean
    public Binding BINDING_QUEUE_INFORM_EMAIL(@Qualifier(QUEUE_INFORM_EMAIL) Queue queue,
                                              @Qualifier(EXCHANGE_TOPICS_INFORM) Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(QUEUE_INFORM_EMAIL).noargs();
    }

    /**
     * 短信队列绑定到交换机，指定routingKey
     * @param queue
     * @param exchange
     * @return
     */
    @Lazy(false)
    @Bean
    public Binding BINDING_QUEUE_INFORM_SMS(@Qualifier(QUEUE_INFORM_SMS) Queue queue,
                                          @Qualifier(EXCHANGE_TOPICS_INFORM) Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(QUEUE_INFORM_SMS).noargs();
    }

    /**
     * 微信公众号队列绑定到交换机，指定routingKey
     * @param queue
     * @param exchange
     * @return
     */
    @Lazy(false)
    @Bean
    public Binding BINDING_QUEUE_INFORM_WECHAT(@Qualifier(QUEUE_INFORM_WECHAT) Queue queue,
                                          @Qualifier(EXCHANGE_TOPICS_INFORM) Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(QUEUE_INFORM_WECHAT).noargs();
    }

    /**
     * 站内信队列绑定到交换机，指定routingKey
     * @param queue
     * @param exchange
     * @return
     */
    @Lazy(false)
    @Bean
    public Binding BINDING_QUEUE_INFORM_INSIDE(@Qualifier(QUEUE_INFORM_INSIDE) Queue queue,
                                          @Qualifier(EXCHANGE_TOPICS_INFORM) Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(QUEUE_INFORM_INSIDE).noargs();
    }

    /**
     * 钉钉h5文本队列绑定到交换机，指定routingKey
     * @param queue
     * @param exchange
     * @return
     */
    @Lazy(false)
    @Bean
    public Binding BINDING_QUEUE_INFORM_DING_H5(@Qualifier(QUEUE_INFORM_DING_H5) Queue queue,
                                          @Qualifier(EXCHANGE_TOPICS_INFORM) Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(QUEUE_INFORM_DING_H5).noargs();
    }
}
