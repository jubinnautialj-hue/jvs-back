package cn.bctools.auth.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.bctools.auth.entity.Job;
import org.apache.ibatis.annotations.CacheNamespace;

/**
 * 岗位基础
 *
 * @author auto
 */
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface JobMapper extends BaseMapper<Job> {
}
