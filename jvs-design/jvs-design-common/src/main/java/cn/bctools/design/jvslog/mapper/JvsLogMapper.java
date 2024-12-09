package cn.bctools.design.jvslog.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.design.jvslog.entity.JvsLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Auto Generator
 */
@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface JvsLogMapper extends BaseMapper<JvsLog> {

}
