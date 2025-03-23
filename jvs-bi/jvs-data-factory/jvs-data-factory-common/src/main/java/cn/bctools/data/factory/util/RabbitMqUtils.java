package cn.bctools.data.factory.util;

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
    public static final String EXCHANGE_TOPICS_DATA_FACTORY_TASK = "exchange_topics_data_factory_task";
    /**
     * 队列名称 任务执行
     */
    public static final String QUEUE_DATA_FACTORY_TASK = "queue_data_factory_task";
    /**
     * 队列名称 消息推送
     */
    public static final String QUEUE_DATA_FACTORY_TASK_NOTICE = "queue_data_factory_task_notice";

    /**
     * 队列名称 血缘视图
     */
    public static final String CONSANGUINITY_ANALYSE_TASK = "consanguinity_analyse_task";

    /**
     * routingKey
     */
    public static final String ROUTING_KEY = "data_factory_routing_key";

    /**
     * 声明交换机
     *
     * @return
     */
    @Bean(EXCHANGE_TOPICS_DATA_FACTORY_TASK)
    @Lazy(false)
    public Exchange exchangeTopicsDataFactoryTask() {
        //durable(true) 持久化，mq重启之后交换机还在
        return ExchangeBuilder.directExchange(EXCHANGE_TOPICS_DATA_FACTORY_TASK).durable(true).build();
    }

    /**
     * 声明队列
     *
     * @return
     */
    @Lazy(false)
    @Bean(QUEUE_DATA_FACTORY_TASK)
    public Queue queueDataFactoryTask() {
        return QueueBuilder.durable(QUEUE_DATA_FACTORY_TASK).build();

    }

    /**
     * 声明队列
     *
     * @return
     */
    @Lazy(false)
    @Bean(QUEUE_DATA_FACTORY_TASK_NOTICE)
    public Queue queueDataFactoryTaskNotice() {
        return new Queue(QUEUE_DATA_FACTORY_TASK_NOTICE);

    }

    /**
     * 声明队列
     *
     * @return
     */
    @Lazy(false)
    @Bean(CONSANGUINITY_ANALYSE_TASK)
    public Queue consanguinityAnalyseTask() {
        return new Queue(CONSANGUINITY_ANALYSE_TASK);

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
    public Binding bindingQueueDataFactoryTaskNotice(@Qualifier(QUEUE_DATA_FACTORY_TASK_NOTICE) Queue queue,
                                                          @Qualifier(EXCHANGE_TOPICS_DATA_FACTORY_TASK) Exchange exchange) {
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
    public Binding bindingConsanguinityAnalyseTask(@Qualifier(CONSANGUINITY_ANALYSE_TASK) Queue queue,
                                                      @Qualifier(EXCHANGE_TOPICS_DATA_FACTORY_TASK) Exchange exchange) {
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
    public Binding bindingQueueDataFactoryTask(@Qualifier(QUEUE_DATA_FACTORY_TASK) Queue queue,
                                                   @Qualifier(EXCHANGE_TOPICS_DATA_FACTORY_TASK) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY).noargs();
    }
}
