package cn.bctools.database.interceptor.cache;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.database.init.DataSourceInit;
import cn.bctools.redis.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.Cache;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * mybatis-redis 二级缓存
 * 通过判断redis 是否存在此key 进行操作.不做序列化操作,做数据静态处理
 *
 * @author zxk
 */
@Slf4j
public class JvsRedisCache implements Cache {

    private final String id;

    public JvsRedisCache(String id) {
        this.id = id;
    }

    public static final String PREFIX = "mybatis_cache:";

    String getPrefix() {
        return PREFIX + SpringContextUtil.getMybatis_cache_prefix();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void putObject(Object key, Object value) {
        if (value != null) {
            RedisUtils bean = getBean();
            //如果没有 变更则直接返回,可能存在部分有缓存,部分没有缓存
            //单独存一份
            bean.setExpire(getPrefix() + key.toString(), value, 60, TimeUnit.MINUTES, RedisSerializer.java());
        }
    }

    static RedisUtils redisUtils = null;

    private static RedisUtils getBean() {
        if (ObjectNull.isNotNull(redisUtils)) {
            return redisUtils;
        }
        redisUtils = SpringContextUtil.getBean(RedisUtils.class);
        return redisUtils;
    }

    /**
     * 通过:MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql生成key
     *
     * @param key The key
     * @return 数据
     */
    @Override
    public Object getObject(Object key) {
        if (key != null) {
            RedisUtils bean = getBean();
            Object o = bean.get(getPrefix() + key, RedisSerializer.java());
            if (o != null) {
                return o;
            } else {
                return null;
            }
        }
        return null;
    }

    @Override
    public Object removeObject(Object key) {
        //触发关联删除
        if (key != null) {
            RedisUtils bean = getBean();
            bean.del(getPrefix() + key.toString());
            linkDeleteMapper();
        }
        return null;
    }

    @Override
    public void clear() {
        log.debug("清空缓存");
        RedisUtils bean = getBean();
        bean.del(bean.keys(getPrefix() + "*" + this.getId() + "*")
                .stream()
                .map(e -> e.substring(RedisUtils.prefix.length()))
                .collect(Collectors.toSet()));
        //触发关联删除
        linkDeleteMapper();
    }

    private void linkDeleteMapper() {
        for (String id : DataSourceInit.MYBATIS_PLUS_CACHE_LINK.keySet()) {
            if (DataSourceInit.MYBATIS_PLUS_CACHE_LINK.get(id).contains(this.getId())) {
                RedisUtils bean = getBean();
                bean.del(bean.keys(getPrefix() + "*" + id + "*")
                        .stream()
                        .map(e -> e.substring(RedisUtils.prefix.length()))
                        .collect(Collectors.toSet()));
                String key = getPrefix() + id;
                bean.del(key);
            }
        }
    }

    @Override
    public int getSize() {
        return 0;
    }

}
