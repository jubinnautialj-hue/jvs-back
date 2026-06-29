package cn.bctools.auth.mapper;

import cn.bctools.auth.entity.OauthOther;
import cn.bctools.database.interceptor.cache.JvsRedisCache;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;

/**
 * @author xh
 */
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface OauthOtherMapper extends BaseMapper<OauthOther> {
}
