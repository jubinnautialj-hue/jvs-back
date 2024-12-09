package cn.bctools.auth.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.bctools.auth.entity.RolePermission;
import org.apache.ibatis.annotations.CacheNamespace;

/**
 * 角色菜单基础mapper
 *
 * @author
 */
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
}
