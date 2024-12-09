package cn.bctools.auth.mapper;

import cn.bctools.auth.entity.Index;
import cn.bctools.auth.entity.Job;
import cn.bctools.database.interceptor.cache.JvsRedisCache;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;

/**
 * @author auto
 */
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface IndexMapper extends BaseMapper<Index> {
}
