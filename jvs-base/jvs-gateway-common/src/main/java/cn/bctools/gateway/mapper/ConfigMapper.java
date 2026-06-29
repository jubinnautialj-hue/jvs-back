package cn.bctools.gateway.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.gateway.entity.SysConfigs;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;

/**
 * @author Auto Generator
 */
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface ConfigMapper extends BaseMapper<SysConfigs> {

}
