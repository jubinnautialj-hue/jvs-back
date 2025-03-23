package cn.bctools.data.factory.source.config;

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
public class RabbitMqConfig {

    /**
     * 交换机名称
     */
    public static final String EXCHANGE_TOPICS_DATA_SOURCE_TASK = "exchange_topics_data_source_task";
    /**
     * 队列名称 任务执行
     */
    public static final String QUEUE_DATA_SOURCE_READ_EXCEL_TASK = "queue_data_source_read_excel_task";

    /**
     * routingKey
     */
    public static final String ROUTING_KEY = "data_source_routing_key";

    /**
     * 声明交换机
     *
     * @return
     */
    @Bean(EXCHANGE_TOPICS_DATA_SOURCE_TASK)
    @Lazy(false)
    public Exchange exchangeTopicsDataFactoryTask() {
        //durable(true) 持久化，mq重启之后交换机还在
        return ExchangeBuilder.directExchange(EXCHANGE_TOPICS_DATA_SOURCE_TASK).durable(true).build();
    }

    /**
     * 声明队列
     *
     * @return
     */
    @Lazy(false)
    @Bean(QUEUE_DATA_SOURCE_READ_EXCEL_TASK)
    public Queue queueDataFactoryTask() {
        return QueueBuilder.durable(QUEUE_DATA_SOURCE_READ_EXCEL_TASK).build();

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
    public Binding bindingQueueDataFactoryTask(@Qualifier(QUEUE_DATA_SOURCE_READ_EXCEL_TASK) Queue queue,
                                                   @Qualifier(EXCHANGE_TOPICS_DATA_SOURCE_TASK) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY).noargs();
    }
}
