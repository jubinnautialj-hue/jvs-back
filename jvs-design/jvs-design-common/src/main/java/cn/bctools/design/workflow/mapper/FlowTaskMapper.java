package cn.bctools.design.workflow.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.design.workflow.dto.PendingApprovesResDto;
import cn.bctools.design.workflow.entity.FlowTask;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;

/**
 * @author zhuxiaokang
 * 工作流任务 Mapper 接口
 */
@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface FlowTaskMapper extends BaseMapper<FlowTask> {

    /**
     * 待我审批分页查询
     * @param page
     * @param wrapper
     * @return
     */
    @Options(useCache = false)
    @Select(" SELECT t.*, taskPerson.node_id, t.courses" +
            " FROM jvs_flow_task_person taskPerson inner JOIN jvs_flow_task t ON taskPerson.flow_task_id = t.id " +
            " ${ew.customSqlSegment} " +
            " ORDER BY t.create_time DESC")
    IPage<PendingApprovesResDto> pendingApprovePage(Page page, @Param("ew") Wrapper wrapper);
}
