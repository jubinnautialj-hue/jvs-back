package cn.bctools.design.crud.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.design.crud.entity.CrudAssociationPo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Auto Generator
 */
@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface JvsCrudAssociationMapper extends BaseMapper<CrudAssociationPo> {

}
