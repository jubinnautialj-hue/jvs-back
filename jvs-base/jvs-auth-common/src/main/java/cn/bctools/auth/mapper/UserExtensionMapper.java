package cn.bctools.auth.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.bctools.auth.entity.UserExtension;
import cn.bctools.auth.entity.UserTenant;
import org.apache.ibatis.annotations.CacheNamespace;

/**
 * @author
 */
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface UserExtensionMapper extends BaseMapper<UserExtension> {
}
