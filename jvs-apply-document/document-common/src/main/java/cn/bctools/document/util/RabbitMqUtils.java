package cn.bctools.document.util;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * rabbitMq工具
 * 声明
 *
 * @author xh
 */
@Lazy(false)
@Configuration
public class RabbitMqUtils {

    /**
     * 交换机名称
     */
    public static final String EXCHANGE_TOPICS_DOCUMENT_TASK = "exchange_topics_document_task";
    /**
     * 队列名称  文件内容同步到es
     */
    public static final String QUEUE_DOCUMENT_FILE_TO_ES_TASK = "queue_data_document_file_to_es_task";
    /**
     * 队列名称  解压文件
     */
    public static final String QUEUE_DOCUMENT_FILE_DECOMPRESSION_TASK = "queue_data_document_file_decompression_task";
    /**
     * 队列名称  上传源文件
     */
    public static final String QUEUE_SOURCE_FILE_UP_TASK = "queue_source_file_up_down_task";
    /**
     * 队列名称  下载源文件
     */
    public static final String QUEUE_SOURCE_FILE_DOWN_TASK = "queue_source_file_down_task";
    /**
     * routingKey
     */
    public static final String ROUTING_KEY = "document_routing_key";

    /**
     * 声明交换机
     *
     * @return
     */
    @Bean(EXCHANGE_TOPICS_DOCUMENT_TASK)
    @Lazy(false)
    public Exchange exchangeTopicsDataDocument() {
        //durable(true) 持久化，mq重启之后交换机还在
        return ExchangeBuilder.directExchange(EXCHANGE_TOPICS_DOCUMENT_TASK).durable(true).build();
    }

    /**
     * 声明队列
     *
     * @return
     */
    @Lazy(false)
    @Bean(QUEUE_DOCUMENT_FILE_TO_ES_TASK)
    public Queue queueDocumentTask() {
        return new Queue(QUEUE_DOCUMENT_FILE_TO_ES_TASK);

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
    public Binding bindingQueueFileToEsTask(@Qualifier(QUEUE_DOCUMENT_FILE_TO_ES_TASK) Queue queue,
                                                   @Qualifier(EXCHANGE_TOPICS_DOCUMENT_TASK) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY).noargs();
    }

    /**
     * 声明队列
     *
     * @return
     */
    @Lazy(false)
    @Bean(QUEUE_DOCUMENT_FILE_DECOMPRESSION_TASK)
    public Queue queueDocumentFileDecompressionTask() {
        return new Queue(QUEUE_DOCUMENT_FILE_DECOMPRESSION_TASK);

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
    public Binding bindingQueueFileDecompressionTask(@Qualifier(QUEUE_DOCUMENT_FILE_DECOMPRESSION_TASK) Queue queue,
                                                   @Qualifier(EXCHANGE_TOPICS_DOCUMENT_TASK) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY).noargs();
    }
    /**
     * 声明队列
     *
     * @return
     */
    @Lazy(false)
    @Bean(QUEUE_SOURCE_FILE_DOWN_TASK)
    public Queue queueSourceFileDownTask() {
        return new Queue(QUEUE_SOURCE_FILE_DOWN_TASK);

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
    public Binding bindingQueueSourceFileDownTask(@Qualifier(QUEUE_SOURCE_FILE_DOWN_TASK) Queue queue,
                                                     @Qualifier(EXCHANGE_TOPICS_DOCUMENT_TASK) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY).noargs();
    }
    /**
     * 声明队列
     *
     * @return
     */
    @Lazy(false)
    @Bean(QUEUE_SOURCE_FILE_UP_TASK)
    public Queue queueSourceFileUpTask() {
        return new Queue(QUEUE_SOURCE_FILE_UP_TASK);

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
    public Binding bindingQueueSourceFileUpTask(@Qualifier(QUEUE_SOURCE_FILE_UP_TASK) Queue queue,
                                                    @Qualifier(EXCHANGE_TOPICS_DOCUMENT_TASK) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY).noargs();
    }
}
