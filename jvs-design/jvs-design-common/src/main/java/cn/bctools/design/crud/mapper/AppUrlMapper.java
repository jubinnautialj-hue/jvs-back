package cn.bctools.design.crud.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.design.crud.entity.AppUrlPo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;


/**
 * 表单配置项
 *
 * @author auto
 */
@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface AppUrlMapper extends BaseMapper<AppUrlPo> {

}
