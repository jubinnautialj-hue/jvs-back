package cn.bctools.design.taskNotice.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.design.taskNotice.entity.FlowTaskNotice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wayne
 * 待办提醒日志 Mapper 接口
 */
@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface FlowTaskNoticeMapper extends BaseMapper<FlowTaskNotice> {
}
