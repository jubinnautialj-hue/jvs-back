package cn.bctools.design.workflow.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.design.workflow.entity.FlowQuickReply;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhuxiaokang
 */

@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface FlowQuickReplyMapper extends BaseMapper<FlowQuickReply> {
}
