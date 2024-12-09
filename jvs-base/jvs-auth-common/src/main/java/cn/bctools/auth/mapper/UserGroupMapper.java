package cn.bctools.auth.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.bctools.auth.entity.UserGroup;
import org.apache.ibatis.annotations.CacheNamespace;

/**
 * @author Administrator
 */
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface UserGroupMapper extends BaseMapper<UserGroup> {


}
