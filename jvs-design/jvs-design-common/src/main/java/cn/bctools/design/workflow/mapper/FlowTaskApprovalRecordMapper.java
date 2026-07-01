package cn.bctools.design.workflow.mapper;

import cn.bctools.database.interceptor.cache.JvsRedisCache;
import cn.bctools.design.workflow.dto.SelfApproveLogResDto;
import cn.bctools.design.workflow.entity.FlowTaskApprovalRecord;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;

/**
 * @author zhuxiaokang
 * 工作流审批记录 Mapper 接口
 */
@Mapper
@CacheNamespace(implementation = JvsRedisCache.class, eviction = JvsRedisCache.class)
public interface FlowTaskApprovalRecordMapper extends BaseMapper<FlowTaskApprovalRecord> {

    /**
     * 我审批的任务记录
     *
     * @param page    分页
     * @param wrapper 查询条件
     * @return
     */
    @Options(useCache = false)
    @Select(" SELECT " +
            "   t.id, " +
            "   t.name, " +
            "   t.task_code, " +
            "   t.title, " +
            "   t.data_model_id, " +
            "   t.form_id, " +
            "   t.data_id, " +
            "   t.task_status, " +
            "   t.stop_reason," +
            "   t.create_by," +
            "   t.create_time," +
            "   t.update_time," +
            "   ar.update_time AS lastApproveTime," +
            "   t.jvs_app_id," +
            "   t.create_by_id" +
            " FROM jvs_flow_task_approval_record ar inner JOIN jvs_flow_task t ON ar.flow_task_id = t.id " +
            " ${ew.customSqlSegment} " +
            " ORDER BY ar.update_time DESC")
    IPage<SelfApproveLogResDto> approvalRecordPage(Page page, @Param("ew") Wrapper wrapper);
}
