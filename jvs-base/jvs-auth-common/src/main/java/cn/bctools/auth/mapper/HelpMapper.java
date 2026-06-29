package cn.bctools.auth.mapper;

import cn.bctools.auth.entity.Help;
import cn.bctools.database.interceptor.cache.JvsRedisCache;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;

/**
 * @author czy
 */
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface HelpMapper extends BaseMapper<Help> {

}
