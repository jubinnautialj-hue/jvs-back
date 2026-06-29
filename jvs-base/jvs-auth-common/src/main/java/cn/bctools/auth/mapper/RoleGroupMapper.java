package cn.bctools.auth.mapper;

import cn.bctools.auth.entity.RoleGroup;
import cn.bctools.database.interceptor.cache.JvsRedisCache;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;

/**
 * 角色分组
 *
 * @author
 */
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface RoleGroupMapper extends BaseMapper<RoleGroup> {
}
