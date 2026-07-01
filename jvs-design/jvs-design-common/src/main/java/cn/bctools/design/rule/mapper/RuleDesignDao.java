package cn.bctools.design.rule.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.design.rule.entity.RuleDesignPo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author guojing
 */
@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface RuleDesignDao extends BaseMapper<RuleDesignPo> {
}
