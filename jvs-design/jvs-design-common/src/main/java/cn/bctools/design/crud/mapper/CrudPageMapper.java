package cn.bctools.design.crud.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.design.crud.entity.CrudPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;


/**
 * 列表的列表配置项
 *
 * @author guojing
 */
@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface CrudPageMapper extends BaseMapper<CrudPage> {

}
