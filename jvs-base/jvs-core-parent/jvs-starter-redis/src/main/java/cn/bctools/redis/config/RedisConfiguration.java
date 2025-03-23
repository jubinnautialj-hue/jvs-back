package cn.bctools.redis.config;

import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.redis.JvsMessageListenerAdapter;
import cn.bctools.redis.cons.CacheCons;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Map;

/**
 * Redis基础配置类
 *
 * @author pangu
 */
@Configuration
@Lazy(value = false)
@EnableCaching
public class RedisConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisSerializer<String> redisKeySerializer() {
        return new Jackson2JsonRedisSerializer(String.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisSerializer<Object> redisValueSerializer() {
        return new Jackson2JsonRedisSerializer(Object.class);
    }

    /**
     * 缓存清除事件
     * 当某个缓存注解{@link org.springframework.cache.annotation.CacheEvict} 触发清除的时候name 触发清楚的时候,默认将自动发送事件,并触发其它关联业务的删除逻辑
     * 例如:
     * 有业务a(权限变更)  业务b,业务a在运行中,新增业务b,不方便修改业务a代码时
     * 业务b需要同时触发相关的缓存清楚事件
     * 如业务a删除数据后和业务b有缓存关联,
     * 则直接使用此种方式即可实现动态关联删除缓存
     * 当角色授权发生变化后,某个业务系统需要同时清空缓存逻辑
     * 使用方式
     * 将需要关联的key存放到{@link CacheCons#putCacheLink(String, String...)}
     * 建议使用此方式 ,不使用{@link org.springframework.cache.annotation.CacheEvict}value 值中的数组,使用数组需要在starter或基础框架中把所有变量写死
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "spring.cache.type", havingValue = "redis")
    JvsMessageListenerAdapter cache(CacheManager cacheManager, JvsCacheConvert jvsCacheConvert) {
        return new JvsMessageListenerAdapter() {
            //关联触发
            @Override
            public void onMessage(String name) {
                //获取消费数据
                CacheCons.CacheDto cacheDto = JSONObject.parseObject(name, CacheCons.CacheDto.class);
                //设置当前线程,可能根据线程删除数据
                TenantContextHolder.setTenantId(cacheDto.getTenantId());
                //查询关联key,
                CacheCons.linkCacheMap.get(cacheDto.getCacheName()).forEach(e -> {
                    //转换缓存key数据
                    String cacheName = jvsCacheConvert.cacheName(e);
                    //批量删除
                    cacheManager.getCache(cacheName).clear();
                });
            }
        };
    }

    /**
     * CacheManager 是一个用于管理缓存的类。在编程中，缓存是一种用于存储并快速访问数据的技术。CacheManager 可以帮助你创建、配置和操作缓存。
     *
     * @param connectionFactory
     * @param cacheInterceptor
     * @return
     */
    @Bean
    @Primary
    @ConditionalOnProperty(value = "spring.cache.type", havingValue = "redis")
    @ConditionalOnMissingBean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory, CacheInterceptor cacheInterceptor, JvsCacheConvert jvsCacheConvert) {
        cacheInterceptor.setKeyGenerator(new JvsCacheKeyGenerator());
        DefaultRedisCacheWriter cacheWriter = new DefaultRedisCacheWriter(connectionFactory, BatchStrategies.keys(), jvsCacheConvert);
        RedisCacheConfiguration defaultCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
        return new RedisCacheManager(cacheWriter, defaultCacheConfiguration) {
            @Override
            public Cache getCache(String name) {
                name = jvsCacheConvert.cacheName(name);
                return super.getCache(name);
            }
        };
    }

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate template = new RedisTemplate();
        //LettuceConnectionFactory
        template.setConnectionFactory(factory);
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(om.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value序列化方式采用jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }


    /**
     * RedisMessageListenerContainer 是 Spring Data Redis 库中的一个类。它用于异步接收 Redis 消息。
     * 你可以将 MessageListener 实例注册到这个容器中，当有新的消息到达时，这些监听器就会被触发。
     *
     * @param connectionFactory
     * @param map
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(RedisMessageListenerContainer.class)
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory,
                                                                       Map<String, JvsMessageListenerAdapter> map) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        if (!map.isEmpty()) {
            map.keySet().forEach(e -> container.addMessageListener(map.get(e), new PatternTopic(e)));
        }
        return container;
    }

    @Bean
    @ConditionalOnMissingBean(JvsCacheConvert.class)
    JvsCacheConvert jvsCacheConvert() {
        return new JvsCacheConvert();
    }


}
