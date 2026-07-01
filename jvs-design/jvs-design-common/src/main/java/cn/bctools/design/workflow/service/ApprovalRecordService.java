package cn.bctools.design.workflow.service;

import cn.bctools.design.workflow.dto.ApprovalRecordFieldDto;

import java.util.List;
import java.util.Map;

/**
 * @author jvs
 * 审批记录服务
 * <p>
 *     此服务负责与审批记录相关的操作
 */
public interface ApprovalRecordService {

    /**
     * 获取流程设计审批记录字段
     *
     * @param flowDesignId 流程设计id
     * @return 审批记录字段集合
     */
    List<ApprovalRecordFieldDto> getApprovalRecordFields(String flowDesignId);

    /**
     * 查询数据指定流程设计最后一次审批记录
     *
     * @param dataId 数据id
     * @return 审批记录。 key：对应"流程设计审批记录字段"中的fieldKey， value：记录数据
     */
    Map<String, Object> getFlowRecord(String dataId);


    /**
     * 查询数据指定流程设计最后一次审批记录
     *
     * @param flowDesignId 流程设计
     * @param dataId 数据id
     * @return 审批记录。 key：对应"流程设计审批记录字段"中的fieldKey， value：记录数据
     */
    Map<String, Object> getFlowRecord(String dataId, String flowDesignId);


}
