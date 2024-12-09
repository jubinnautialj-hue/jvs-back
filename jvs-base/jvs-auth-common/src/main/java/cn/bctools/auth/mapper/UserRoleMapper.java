package cn.bctools.auth.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.bctools.auth.entity.UserRole;
import org.apache.ibatis.annotations.CacheNamespace;

/**
 * 用户角色基础服务
 * @author
 */
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface UserRoleMapper extends BaseMapper<UserRole> {
}
