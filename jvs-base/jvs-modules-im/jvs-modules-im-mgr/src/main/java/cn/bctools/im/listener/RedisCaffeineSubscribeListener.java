package cn.bctools.im.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jim.core.cache.CacheChangeType;
import org.jim.core.cache.CacheChangedVo;
import org.jim.core.cache.caffeineredis.CaffeineRedisCache;
import org.jim.core.cache.caffeineredis.CaffeineRedisCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Author: ZhuXiaoKang
 * @Description: 订阅
 */
@Slf4j
@Component
public class RedisCaffeineSubscribeListener implements MessageListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        log.debug("接收到redis发布的消息,chanel:{},message :{}",message.getChannel(), message);
        String content = (String)redisTemplate.getValueSerializer().deserialize(message.getBody());
        if (StringUtils.isBlank(content)) {
            log.error("redis消费消息为空");
            return;
        }
        String[] cacheChanges = content.split(":");
        String cacheName = cacheChanges[0];
        String key = cacheChanges[1];
        CacheChangeType type = CacheChangeType.from(Integer.valueOf(cacheChanges[2]));
        String clientid = cacheChanges[3];
        if (StringUtils.isBlank(clientid)) {
            log.error("clientid is null");
            return;
        }
        if (Objects.equals(CacheChangedVo.CLIENTID, clientid)) {
            log.debug("自己发布的消息,{}", clientid);
            return;
        }
        CaffeineRedisCache caffeineRedisCache = CaffeineRedisCacheManager.getCache(cacheName);
        if (caffeineRedisCache == null) {
            log.debug("不能根据cacheName[{}]找到CaffeineRedisCache对象", cacheName);
            return;
        }
        if (type == CacheChangeType.PUT  || type == CacheChangeType.UPDATE || type == CacheChangeType.REMOVE) {
            caffeineRedisCache.getCaffeineCache().remove(key);
        } else if (type == CacheChangeType.CLEAR) {
            caffeineRedisCache.getCaffeineCache().clear();
        }
    }
}
