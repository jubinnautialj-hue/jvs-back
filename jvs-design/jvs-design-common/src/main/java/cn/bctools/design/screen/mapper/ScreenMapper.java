package cn.bctools.design.screen.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.design.screen.entity.ScreenPo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wl
 */
@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface ScreenMapper extends BaseMapper<ScreenPo> {

}
