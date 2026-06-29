package cn.bctools.im.config;

import cn.bctools.im.listener.RedisCaffeineSubscribeListener;
import org.jim.core.cache.caffeineredis.CaffeineRedisCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * @Author: ZhuXiaoKang
 * @Description: redis订阅配置
 */
@Configuration
public class RedisSubscribeConfig {

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory redisConnectionFactory, RedisCaffeineSubscribeListener redisCaffeineSubscribeListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        // 订阅频道
        container.addMessageListener(redisCaffeineSubscribeListener, new ChannelTopic(CaffeineRedisCacheManager.CACHE_CHANGE_TOPIC));
        return container;
    }
}
