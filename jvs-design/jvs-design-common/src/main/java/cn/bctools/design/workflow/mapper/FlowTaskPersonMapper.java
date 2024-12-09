package cn.bctools.design.workflow.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.design.workflow.entity.FlowTaskPerson;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhuxiaokang
 * 工作流任务待办人 Mapper 接口
 */
@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface FlowTaskPersonMapper extends BaseMapper<FlowTaskPerson> {

}
