package cn.bctools.auth.mapper;

import cn.bctools.auth.entity.DeptRole;
import cn.bctools.database.interceptor.cache.JvsRedisCache;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;

/**
 * @author zhuxiaokang
 */
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface DeptRoleMapper extends BaseMapper<DeptRole> {
}
