package cn.bctools.auth.mapper;

import cn.bctools.auth.entity.UserLevel;
import cn.bctools.database.interceptor.cache.JvsRedisCache;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;

/**
 * @author
 */
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface UserLevelMapper extends BaseMapper<UserLevel> {

}
