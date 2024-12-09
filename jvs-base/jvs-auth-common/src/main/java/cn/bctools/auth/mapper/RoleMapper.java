package cn.bctools.auth.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.bctools.auth.entity.Role;
import org.apache.ibatis.annotations.CacheNamespace;

/**
 * 基础角色
 * @author
 */
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface RoleMapper extends BaseMapper<Role> {
}
