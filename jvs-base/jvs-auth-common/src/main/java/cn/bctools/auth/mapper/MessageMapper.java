package cn.bctools.auth.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.bctools.auth.entity.Message;
import org.apache.ibatis.annotations.CacheNamespace;

/**
 * @author guojing
 */
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface MessageMapper extends BaseMapper<Message> {

}
