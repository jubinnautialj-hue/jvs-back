package cn.bctools.design.external.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.design.external.entity.ExternalPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhuxiaokang
 * 外部页面配置 Mapper 接口
 */
@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface ExternalPageMapper extends BaseMapper<ExternalPage> {

}
