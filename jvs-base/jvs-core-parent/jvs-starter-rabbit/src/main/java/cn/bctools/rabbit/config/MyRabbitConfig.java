package cn.bctools.rabbit.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * @author Administrator
 */
@EnableRabbit
@Lazy(false)
@Configuration
public class MyRabbitConfig {

    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(om);
    }

    /**
     * 批量消费
     */
    public static final String BATCH_LISTENER_CONTAINER_FACTORY = "batchQueueRabbitListenerContainerFactory";


    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        // 配置序列化方式
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        // 自动创建交换机、队列
        return new RabbitAdmin(connectionFactory);
    }

    @Bean(BATCH_LISTENER_CONTAINER_FACTORY)
    public SimpleRabbitListenerContainerFactory batchQueueRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        // 设置批量
        factory.setBatchListener(true);
        // 设置BatchMessageListener生效
        factory.setConsumerBatchEnabled(true);
        //设置为手工确认模式
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        // 设置监听器一次批量处理的消息数量
        factory.setBatchSize(50);
        return factory;
    }

}