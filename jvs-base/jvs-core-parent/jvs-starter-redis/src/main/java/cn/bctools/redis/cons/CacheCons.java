package cn.bctools.redis.cons;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.AccessType;

import java.util.*;

/**
 * 缓存公共变量变量
 *
 * @author gk
 */
public class CacheCons {

    /**
     * 用户缓存
     */
    public static final String CACHE_USER_PREFIX = "cache_user";

    public static Map<String, Set<String>> linkCacheMap = new HashMap<>(8);

    /**
     * 创建需要关联的缓存数据
     *
     * @param cacheName     现有关联的数据
     * @param linkCacheName 业务需要同步删除的缓存name
     */
    public static void putCacheLink(String cacheName, String... linkCacheName) {
        Set<String> linkCacheMapOrDefault = linkCacheMap.getOrDefault(cacheName, new HashSet<>(linkCacheName.length));
        for (String s : linkCacheName) {
            linkCacheMapOrDefault.add(s);
        }
        linkCacheMap.put(cacheName, linkCacheMapOrDefault);
    }

    /**
     * 事件触发时发送的对象
     */
    @Data
    @Accessors(chain = true)
    public static class CacheDto {
        String tenantId;
        String cacheName;
    }

}
