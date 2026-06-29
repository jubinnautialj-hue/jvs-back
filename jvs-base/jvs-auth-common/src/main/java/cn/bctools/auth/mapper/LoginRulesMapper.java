package cn.bctools.auth.mapper;

import cn.bctools.auth.entity.LoginRules;
import cn.bctools.database.interceptor.cache.JvsRedisCache;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;

/**
 * @author auto
 */
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface LoginRulesMapper extends BaseMapper<LoginRules> {
}
