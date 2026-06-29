package cn.bctools.auth.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.bctools.auth.entity.SysDict;
import org.apache.ibatis.annotations.CacheNamespace;

/**
 * <p>
 * 字典表 Mapper 接口
 * </p>
 *
 * @author guojing
 */
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface SysDictMapper extends BaseMapper<SysDict> {

}
