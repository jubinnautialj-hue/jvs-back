package cn.bctools.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class MyKafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:jvs-kafka:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.retries:3}")
    private Integer retries;

    @Value("${spring.kafka.producer.batch-size:1000}")
    private Integer batchSize;

    @Value("${spring.kafka.producer.buffer-memory:33554432}")
    private Integer bufferMemory;

    @Value("${spring.kafka.consumer.group-id:jvs-kafka}")
    private String groupId;

    @Value("${spring.kafka.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;

    @Value("${spring.kafka.consumer.max-poll-records:4000}")
    private Integer maxPollRecords;

    @Value("${spring.kafka.consumer.batch.concurrency:1}")
    private Integer batchConcurrency;

    @Value("${spring.kafka.consumer.enable-auto-commit:true}")
    private Boolean autoCommit;

    @Value("${spring.kafka.consumer.auto-commit-interval:1000}")
    private Integer autoCommitInterval;

    @Value("${spring.kafka.producer.properties.sasl.mechanism:PLAIN}")
    private String producerMechanism;
    @Value("${spring.kafka.producer.properties.security.protocol:SASL_PLAINTEXT}")
    private String producerProtocol;
    @Value("${spring.kafka.producer.properties.sasl.jaas.config: org.apache.kafka.common.security.plain.PlainLoginModule required username='cxy' password='654321';}")
    private String producerJaasConfig;
    @Value("${spring.kafka.consumer.properties.sasl.mechanism:PLAIN}")
    private String consumerMechanism;
    @Value("${spring.kafka.consumer.properties.security.protocol:SASL_PLAINTEXT}")
    private String consumerProtocol;
    @Value("${spring.kafka.consumer.properties.sasl.jaas.config: org.apache.kafka.common.security.plain.PlainLoginModule required username='cxy' password='654321';}")
    private String consumerJaasConfig;

    @Value("${spring.kafka.producer.sasl.enable: true}")
    private Boolean producerSaslStatus;

    @Value("${spring.kafka.consumer.sasl.enable: true}")
    private Boolean consumerSaslStatus;

    /**
     * 批量消费
     */
    public static final String BATCH_LISTENER_CONTAINER_FACTORY = "batchKafkaListenerContainerFactory";

    /**
     *  生产者配置信息
     */
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.ACKS_CONFIG, "0");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        if (producerSaslStatus) {
            props.put("security.protocol", producerProtocol);
            props.put("sasl.mechanism", producerMechanism);
            props.put("sasl.jaas.config", producerJaasConfig);
        }
        return props;
    }

    /**
     *  生产者工厂
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    /**
     *  生产者模板
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }


    /**
     *  消费者配置信息
     */
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, autoCommitInterval);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        if (consumerSaslStatus) {
            props.put("security.protocol", consumerProtocol);
            props.put("sasl.mechanism", consumerMechanism);
            props.put("sasl.jaas.config", consumerJaasConfig);
        }
        return props;
    }

    /**
     *  消费者批量工厂
     */
    @Bean(BATCH_LISTENER_CONTAINER_FACTORY)
    public KafkaListenerContainerFactory<?> batchFactory() {
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        Map<String, Object> objectMap = consumerConfigs();
        objectMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,false);
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(objectMap));
        //设置并发量，小于或等于Topic的分区数
        factory.setConcurrency(batchConcurrency);
        factory.getContainerProperties().setPollTimeout(1500);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        //设置为批量消费，每个批次数量在Kafka配置参数中设置ConsumerConfig.MAX_POLL_RECORDS_CONFIG
        factory.setBatchListener(true);
        return factory;
    }

}
