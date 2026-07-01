package cn.bctools.design.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import lombok.Data;
import org.springframework.boot.autoconfigure.mongo.MongoClientFactory;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.autoconfigure.mongo.MongoPropertiesClientSettingsBuilderCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.util.Collections;

/**
 * @author hlk
 * mongodb多数据源配置。 根据模式分库（开发、测试、正式）
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.data.mongodb")
public class MultipleMongoConfig {

    private MongoProperties dev;
    private MongoProperties beta;

    @Bean(name = "devMongoTemplate")
    public MongoTemplate devMongoTemplate(MultipleMongoConfig properties, Environment environment) {
        MongoProperties mongoProperties = properties.getDev();
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient(mongoProperties, environment), mongoProperties.getDatabase()));
    }

    @Bean(name = "betaMongoTemplate")
    public MongoTemplate betaMongoTemplate(MultipleMongoConfig properties, Environment environment) {
        MongoProperties mongoProperties = properties.getBeta();
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient(mongoProperties, environment), mongoProperties.getDatabase()));
    }

    @Primary
    @Bean(name = "gaMongoTemplate")
    public MongoTemplate gaMongoTemplate(MongoProperties properties, Environment environment) {
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient(properties, environment), properties.getDatabase()));
    }

    private MongoClient mongoClient(MongoProperties properties, Environment environment) {
        MongoPropertiesClientSettingsBuilderCustomizer customizer = new MongoPropertiesClientSettingsBuilderCustomizer(properties, environment);
        return new MongoClientFactory(Collections.singletonList(customizer)).createMongoClient(MongoClientSettings.builder().build());
    }
}
