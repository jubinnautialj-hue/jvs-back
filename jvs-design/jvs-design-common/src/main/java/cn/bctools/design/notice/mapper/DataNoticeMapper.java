package cn.bctools.design.notice.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.design.notice.entity.DataNoticePo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhuxiaokang
 * 消息通知
 */
@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface DataNoticeMapper extends BaseMapper<DataNoticePo> {
}
