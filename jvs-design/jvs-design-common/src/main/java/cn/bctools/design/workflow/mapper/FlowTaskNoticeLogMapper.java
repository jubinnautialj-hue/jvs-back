package cn.bctools.design.workflow.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.design.workflow.entity.FlowTaskNoticeLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wayne
 * 待办提醒日志 Mapper 接口
 */
@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface FlowTaskNoticeLogMapper extends BaseMapper<FlowTaskNoticeLog> {
}
