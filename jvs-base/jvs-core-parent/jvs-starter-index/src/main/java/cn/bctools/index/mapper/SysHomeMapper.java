package cn.bctools.index.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.index.entity.SysHome;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 首页数据 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2023-03-16
 */
@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface SysHomeMapper extends BaseMapper<SysHome> {

}
