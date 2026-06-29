package org.jim.core.utils;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
public class RedisUtil {

    static StringRedisTemplate redisTemplate = SpringContextUtils.getBean(StringRedisTemplate.class);

    public static void setRedisTemplate(StringRedisTemplate redisTemplate) {
        RedisUtil.redisTemplate = redisTemplate;
    }

    /**
     * 写入缓存
     *
     * @param key    缓存key
     * @param value  缓存值
     * @param expire 过期时间
     * @param unit   过期类型
     */
    public static Boolean set(final String key, final Object value, long expire, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, JSONObject.toJSONString(value), expire, unit);
        return true;
    }

    /**
     * 存放一个缓存
     *
     * @param key    缓存key
     * @param value  缓存值
     * @param expire 过期时间
     * @param unit   过期类型
     * @author: guojing
     * @return: 如果不存在，则返回
     */
    public static Boolean set(final String key, final String value, long expire, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, expire, unit);
        return true;
    }

    /**
     * 存放一个缓存
     *
     * @param key   缓存key
     * @param value 缓存值
     *              永不过期
     * @author: guojing
     * @return: void
     */
    public static Boolean set(final String key, final Object value) {
        redisTemplate.opsForValue().set(key, JSONObject.toJSONString(value));
        return true;
    }

    /**
     * 存放一个缓存
     * 永不过期
     *
     * @param key   缓存key
     * @param value 缓存值
     * @author: guojing
     * @return: void
     */
    public static Boolean set(final String key, final String value) {
        //如果不存在，则存放
        redisTemplate.opsForValue().set(key, value);
        return true;
    }

    /**
     * 读取一个缓存数据
     *
     * @param key 数据缓存key
     * @author: guojing
     * @return: T
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(final String key) {
        return (T) redisTemplate.boundValueOps(key).get();
    }

    /**
     * 读取缓存数据对象,直接映射实体类
     *
     * @param key 缓存key
     * @param cls 映射对象
     * @author: guojing
     * @return: T
     */
    public static <T> T get(final String key, Class<T> cls) {
        return JSONObject.parseObject(redisTemplate.boundValueOps(key).get(), cls);
    }

    /**
     * 向集合推送数据
     *
     * @param key 缓存对象
     * @param obj 存放的数据对象
     * @author: guojing
     * @return: java.lang.Long
     */
    public static Long putList(String key, Object obj) {
        //如果是字段串，直接保存，如果不是，则格式化为对象保存
        String value = (obj instanceof String) ? String.valueOf(obj) : JSONObject.toJSONString(obj);
        return redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * 批量向集合推送数据
     *
     * @param key 缓存对象
     * @param obj 存放的数据对象
     * @return: java.lang.Long
     */
    public static Long putListAll(String key, Object... obj) {
        String value = JSONObject.toJSONString(obj);
        List<String> values = JSON.parseObject(value, List.class);
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    /**
     * 获取redis list类型所有的数据，直接返回集合类型,这个直接使用的fastjson格式化
     *
     * @param key 缓存key
     * @param cls 需要映射的实体类值
     * @author: guojing
     * @return: java.util.List
     */
    public static <T> List<T> getList(String key, Class<T> cls) {
        //把所有的都给拿出来
        return redisTemplate.opsForList().range(key, 0, -1).stream().map(e -> JSONObject.parseObject(e, cls)).collect(Collectors.toList());
    }

    /**
     * 获取redis list类型所有的数据
     *
     * @param key 字段说明字段说明
     * @author: guojing
     * @return: java.util.List
     */
    public static List<String> getList(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 删除，根据key精确匹配
     *
     * @param key
     */
    /**
     * 模糊删除key
     *
     * @param key 字段说明
     * @author: guojing
     * @return: void
     */
    public static void delKey(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 模糊删除key
     *
     * @param key 字段说明
     * @author: guojing
     * @return: void
     */
    public static void delKey(final String... key) {
        redisTemplate.delete(Arrays.asList(key));
    }

    /**
     * 从集合中删除值为value的指定元素
     *
     * @param key 缓存key
     * @param count count> 0：删除等于从左到右移动的值的第一个元素；count< 0：删除等于从右到左移动的值的第一个元素；count = 0：删除等于value的所有元素
     * @param value 值
     * @return java.lang.Long
     */
    public static long listRemove(final String key , long count , final String value) {
        return redisTemplate.opsForList().remove(key, count, value);
    }

    /**
     * 修改一个数据key的时效性
     *
     * @param key     缓存key
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return boolean 是否设置成功
     */
    public static boolean expire(String key, final long timeout, final TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 通过key
     * 右模糊匹配查询
     *
     * @param key 缓存key
     * @author: guojing
     * @return: java.lang.String[]  返回的缓存key
     */
    public static String[] matchKeyRight(final String key) {
        Set<String> keys = redisTemplate.keys(key + "*");
        return keys.toArray(new String[keys.size()]);
    }


    /**
     * 通过key
     * 全模糊匹配查询
     *
     * @param key 缓存key
     * @author: guojing
     * @return: java.lang.String[]  返回的缓存key
     */
    public static String[] matchKey(final String key) {
        Set<String> keys = redisTemplate.keys("*" + key + "*");
        return keys.toArray(new String[keys.size()]);
    }

    /**
     * 通过key
     * 左模糊匹配查询
     *
     * @param key 缓存key
     * @author: guojing
     * @return: java.lang.String[]  返回的缓存key
     */
    public static String[] matchKeyLeft(final String key) {
        Set<String> keys = redisTemplate.keys("*" + key);
        return keys.toArray(new String[keys.size()]);
    }

    /**
     * 获取一批的缓存对象，
     *
     * @param keys 多个缓存key
     * @author: guojing
     * @return: java.util.List<T>
     */
    public static List keys(String... keys) {
        return Arrays.stream(keys).map(v -> get(v)).collect(Collectors.toList());
    }

    /**
     * 获取一批的缓存对象，
     *
     * @param cls  字段说明
     * @param keys 多个缓存key
     * @author: guojing
     * @return: java.util.List<T>
     */
    public static <T> List<T> keys(Class<T> cls, String... keys) {
        return Arrays.stream(keys).map(v -> get(v, cls)).collect(Collectors.toList());
    }

    /**
     * 判断是否有这个缓存key
     *
     * @param key 缓存key
     * @author: guojing
     * @return: boolean
     */
    public static boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 将redis缓存key的值+1
     *
     * @param key 缓存key
     * @return value+1后的值
     */
    public static Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 将redis缓存key的值增加指定的值
     *
     * @param key   缓存key
     * @param delta 递增的步长
     * @return value+N后的值
     */
    public static Long increment(String key, Long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 将redis缓存key的值增加指定的值
     *
     * @param key   缓存key
     * @param delta 递增的步长
     * @return value+N后的值
     */
    public static Double increment(String key, Double delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 将redis缓存key的值减少1
     *
     * @param key 缓存key
     * @return value-1后的值
     */
    public static Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    /**
     * 将redis缓存key的值减少指定的值
     *
     * @param key 缓存key
     * @return value-N后的值
     */
    public static Long decrement(String key, Long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    /**
     * 返回redis 指定key 的剩余ttl时间; 特殊情况  -1 表示 key 存在，但没有设置剩余生存时间  -2表示key 不存在
     *
     * @param key 缓存key
     * @return 剩余的ttl时间 单位:秒
     */
    public static Long ttl(String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * 返回redis 指定key 的剩余ttl时间; 特殊情况  -1 表示 key 存在，但没有设置剩余生存时间  -2表示key 不存在
     *
     * @param key 缓存key
     * @return 剩余的ttl时间 单位:调用时指定的单位
     */
    public static Long ttl(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }

    /**
     * 向有序Set推送数据
     *
     * @param key 缓存对象
     * @param obj 存放的数据对象
     * @param score 分值
     * @return: java.lang.Long
     */
    public static Boolean addZset(String key, Object obj, double score) {
        //如果是字段串，直接保存，如果不是，则格式化为对象保存
        String value = (obj instanceof String) ? String.valueOf(obj) : JSONObject.toJSONString(obj);
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 根据分数区间获取redis zset类型的数据
     *
     * @param key 缓存key
     * @param minScore 分数区间最小值
     * @param maxScore 分数区间最大值
     * @param cls 需要映射的实体类值
     * @return: java.util.Set
     */
    public static <T> Set<T> getZsetRangeByScore(String key, double minScore, double maxScore, Class<T> cls) {
        return redisTemplate.opsForZSet().rangeByScore(key, minScore, maxScore).stream().map(e -> JSONObject.parseObject(e, cls)).collect(Collectors.toSet());
    }

    /**
     * 根据分数区间获取redis zset类型的数据
     *
     * @param key 缓存key
     * @param minScore 分数区间最小值
     * @param maxScore 分数区间最大值
     * @return: java.util.List
     */
    public static Set<String> getZsetRangeByScore(String key, double minScore, double maxScore) {
        return redisTemplate.opsForZSet().rangeByScore(key, minScore, maxScore);
    }

    /**
     * 根据分数区间和指定下标，指定长度，获取redis zset类型的数据
     *
     * @param key 缓存key
     * @param minScore 分数区间最小值
     * @param maxScore 分数区间最大值
     * @param offset 下标
     * @param count 长度
     * @param cls 需要映射的实体类值
     * @return: java.util.Set
     */
    public static <T> Set<T> getZsetRangeByScore(String key, double minScore, double maxScore, long offset, long count, Class<T> cls) {
        return redisTemplate.opsForZSet().rangeByScore(key, minScore, maxScore, offset, count).stream().map(e -> JSONObject.parseObject(e, cls)).collect(Collectors.toSet());
    }

    /**
     * 根据分数区间和指定下标，指定长度，获取redis zset类型的数据
     *
     * @param key 字段说明字段说明
     * @param minScore 分数区间最小值
     * @param maxScore 分数区间最大值
     * @param offset 下标
     * @param count 长度
     * @return: java.util.List
     */
    public static Set<String> getZsetRangeByScore(String key, double minScore, double maxScore, long offset, long count) {
        return redisTemplate.opsForZSet().rangeByScore(key, minScore, maxScore, offset, count);
    }

    /**
     * 写入hash缓存
     *
     * @param key    缓存key
     * @param map 数据
     * @param expire 过期时间
     * @param unit   过期类型
     */
    public static Boolean hPutAll(final String key, Map<String, Object> map, long expire, TimeUnit unit) {
        hPutAll(key, map);
        expire(key, expire, unit);
        return true;
    }

    /**
     * 写入hash缓存
     *
     * @param key    缓存key
     * @param map 数据
     */
    public static Boolean hPutAll(final String key, Map<String, Object> map) {
        if (map == null) {
            return true;
        }
        Map<String, Object> m = map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, v -> JSONObject.toJSONString(v.getValue())));
        redisTemplate.opsForHash().putAll(key, m);
        return true;
    }

    /**
     * 单个写入hash缓存
     *
     * @param key    缓存key
     * @param hashKey map的key
     * @param value  缓存值
     */
    public static Boolean hPut(final String key, final String hashKey, final Object value) {
        String jsonVal = JSONUtil.isJson(JSON.toJSONString(value)) ? JSONUtil.toJsonStr(value) : String.valueOf(value);
        redisTemplate.opsForHash().put(key, hashKey, jsonVal);
        return true;
    }

    /**
     * 获取指定hash
     * @param key
     * @return
     */
    public static Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 获取指定hash的指定值
     * @param key 缓存key
     * @param hashKey map的key
     * @param cls 需要映射的实体类值
     * @return
     */
    public static <T> T hGet(String key, String hashKey, Class<T> cls) {
        Object obj = redisTemplate.opsForHash().get(key, hashKey);
        if (obj == null) {
            return null;
        }
        return JSONUtil.toBean(JSONUtil.toJsonStr(obj), cls);
    }

    /**
     * 删除hash的指定值
     * @param key 缓存key
     * @param hashKeys map的key
     */
    public static void hDelete(String key, String... hashKeys) {
        redisTemplate.opsForHash().delete(key, hashKeys);
    }




}
