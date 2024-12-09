package cn.bctools.auth.mapper;

import cn.bctools.auth.entity.Bulletin;
import cn.bctools.auth.entity.IndexApplet;
import cn.bctools.database.interceptor.cache.JvsRedisCache;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;

/**
 * @author zhuxiaokang
 * 系统公告 Mapper 接口
 */
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface IndexAppletMapper extends BaseMapper<IndexApplet> {

}
