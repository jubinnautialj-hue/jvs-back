package cn.bctools.function.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.function.entity.po.FunctionBusinessPo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author czy
 */
@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface FunctionBusinessMapper extends BaseMapper<FunctionBusinessPo> {

}
