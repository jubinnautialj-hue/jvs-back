package cn.bctools.design.crud.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.design.crud.entity.PrintTemplate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhuxiaokang
 * 打印模板 Mapper 接口
 */
@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface PrintTemplateMapper extends BaseMapper<PrintTemplate> {
}
