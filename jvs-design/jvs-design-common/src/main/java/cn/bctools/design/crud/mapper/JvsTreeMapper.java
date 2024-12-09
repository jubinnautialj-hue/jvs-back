package cn.bctools.design.crud.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.design.crud.entity.JvsTree;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;

/**
 * @author : GaoZeXi
 */
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface JvsTreeMapper extends BaseMapper<JvsTree> {
}
