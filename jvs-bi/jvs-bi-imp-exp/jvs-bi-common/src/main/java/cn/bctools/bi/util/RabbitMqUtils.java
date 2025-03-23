package cn.bctools.bi.util;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * rabbitMq工具
 *
 * @author xh
 */
@Lazy(false)
@Configuration
public class RabbitMqUtils {

    /**
     * 交换机名称
     */
    public static final String EXCHANGE_TOPICS_BI_TASK = "exchange_topics_bi_task";
    /**
     * 队列名称 任务执行
     */
    public static final String QUEUE_BI_TASK = "queue_bi_task";
    /**
     * 队列名称 设计数据下载
     */
    public static final String QUEUE_BI_TASK_DOWN = "queue_bi_down_task";

    /**
     * 队列名称 上传
     */
    public static final String QUEUE_BI_TASK_UP = "queue_bi_up_task";

    /**
     * routingKey
     */
    public static final String ROUTING_KEY = "bi_routing_key";

    /**
     * 声明交换机
     *
     * @return
     */
    @Bean(EXCHANGE_TOPICS_BI_TASK)
    @Lazy(false)
    public Exchange exchangeTopicsDataFactoryTask() {
        //durable(true) 持久化，mq重启之后交换机还在
        return ExchangeBuilder.directExchange(EXCHANGE_TOPICS_BI_TASK).durable(true).build();
    }

    /**
     * 声明队列
     *
     * @return
     */
    @Lazy(false)
    @Bean(QUEUE_BI_TASK)
    public Queue queueDataFactoryTask() {
        return QueueBuilder.durable(QUEUE_BI_TASK).build();

    }

    /**
     * 声明队列
     *
     * @return
     */
    @Lazy(false)
    @Bean(QUEUE_BI_TASK_DOWN)
    public Queue queueDataFactoryTaskNotice() {
        return new Queue(QUEUE_BI_TASK_DOWN);

    }

    /**
     * 声明队列
     *
     * @return
     */
    @Lazy(false)
    @Bean(QUEUE_BI_TASK_UP)
    public Queue consanguinityAnalyseTask() {
        return new Queue(QUEUE_BI_TASK_UP);

    }

    /**
     * 队列绑定交换机，指定routingKey
     *
     * @param queue
     * @param exchange
     * @return
     */
    @Lazy(false)
    @Bean
    public Binding bindingQueueDataFactoryTaskNotice(@Qualifier(QUEUE_BI_TASK_DOWN) Queue queue,
                                                     @Qualifier(EXCHANGE_TOPICS_BI_TASK) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY).noargs();
    }

    /**
     * 队列绑定交换机，指定routingKey
     *
     * @param queue
     * @param exchange
     * @return
     */
    @Lazy(false)
    @Bean
    public Binding bindingConsanguinityAnalyseTask(@Qualifier(QUEUE_BI_TASK_UP) Queue queue,
                                                   @Qualifier(EXCHANGE_TOPICS_BI_TASK) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY).noargs();
    }

    /**
     * 队列绑定交换机，指定routingKey
     *
     * @param queue
     * @param exchange
     * @return
     */
    @Lazy(false)
    @Bean
    public Binding bindingQueueDataFactoryTask(@Qualifier(QUEUE_BI_TASK) Queue queue,
                                               @Qualifier(EXCHANGE_TOPICS_BI_TASK) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY).noargs();
    }
}
