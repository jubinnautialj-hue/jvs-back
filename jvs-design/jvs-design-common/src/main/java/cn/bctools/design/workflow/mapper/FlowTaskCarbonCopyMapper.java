package cn.bctools.design.workflow.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.design.workflow.dto.CarbonCopyResDto;
import cn.bctools.design.workflow.entity.FlowTaskCopied;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;

/**
 * @author zhuxiaokang
 */
@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface FlowTaskCarbonCopyMapper extends BaseMapper<FlowTaskCopied> {

    /**
     * 根据用户查询抄送的信息
     *
     * @param page    分页对象
     * @param userId  用户对象
     * @param wrapper 动态查询条件
     * @return
     */
    @Options(useCache = false)
    @Select(" SELECT t.*, cc.create_time AS carbonCopyTime " +
            " FROM jvs_flow_task_copied cc inner JOIN jvs_flow_task t ON cc.flow_task_id = t.id " +
            " ${ew.customSqlSegment} " +
            " ORDER BY cc.create_time DESC")
    IPage<CarbonCopyResDto> carbonCopyPage(Page page, @Param("userId") String userId, @Param("ew") Wrapper wrapper);
}
