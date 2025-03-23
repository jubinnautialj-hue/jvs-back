package cn.bctools.rule.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.rule.po.RuleOptionPo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Administrator
 */
@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface RuleOptionDao extends BaseMapper<RuleOptionPo> {
}
