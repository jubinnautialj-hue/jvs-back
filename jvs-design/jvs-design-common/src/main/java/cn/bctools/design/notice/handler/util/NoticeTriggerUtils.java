package cn.bctools.design.notice.handler.util;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.notice.handler.bo.TriggerSettingBo;
import cn.bctools.design.notice.handler.bo.trigger.EditedCondition;
import cn.bctools.design.notice.handler.bo.trigger.FlowApprovalNodeCondition;
import cn.bctools.design.notice.handler.bo.trigger.FlowApprovalResultsCondition;
import cn.bctools.design.notice.handler.enums.TriggerEditedConditionTypeEnum;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import com.alibaba.fastjson2.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;

/**
 * @author zhuxiaokang
 * 消息通知触发条件工具
 */
public class NoticeTriggerUtils {

    private NoticeTriggerUtils() {

    }

    /**
     * 校验“编辑成功”是否满足发送消息通知条件
     *
     * @param triggerSetting 触发条件配置
     * @param changeKeys 变更的字段集合
     * @return TRUE-满足发消息条件，FALSE-不满足发消息条件
     */
    public static Boolean checkTriggerEdited(TriggerSettingBo triggerSetting, Collection<String> changeKeys) {
        // 没有变更，视为不满足发送消息条件
        if (ObjectNull.isNull(changeKeys)) {
            return Boolean.FALSE;
        }
        if(StringUtils.isBlank(triggerSetting.getCondition())) {
            return Boolean.TRUE;
        }
        EditedCondition condition = JSON.parseObject(triggerSetting.getCondition(), EditedCondition.class);
        // 任意字段编辑成功，直接通过校验
        if (TriggerEditedConditionTypeEnum.ANY.equals(condition.getType())) {
            return Boolean.TRUE;
        }
        // 校验指定字段编辑成功
        List<String> fieldKeys = condition.getFieldKeys();
        if(CollectionUtils.isEmpty(fieldKeys)) {
            return Boolean.FALSE;
        }
        return changeKeys.stream().anyMatch(fieldKeys::contains);
    }

    /**
     * 校验“审批结果”是否满足发送消息通知条件
     *
     * @param triggerSetting 触发条件配置
     * @param flowTask 工作流任务
     * @return TRUE-满足发消息条件，FALSE-不满足发消息条件
     */
    public static Boolean checkTriggerFlowApprovalResults(TriggerSettingBo triggerSetting, FlowTask flowTask) {
        if(StringUtils.isBlank(triggerSetting.getCondition())) {
            return Boolean.FALSE;
        }
        FlowApprovalResultsCondition condition = JSON.parseObject(triggerSetting.getCondition(), FlowApprovalResultsCondition.class);
        if (CollectionUtils.isEmpty(condition.getFlowTaskStatus())) {
            return Boolean.FALSE;
        }
        return condition.getFlowTaskStatus().stream().anyMatch(e -> e.equals(flowTask.getTaskStatus().getValue()));
    }

    /**
     * 校验“审批节点”是否满足发送消息通知条件
     *
     * @param triggerSetting 触发条件配置
     * @param flowTaskNodes 待处理的工作流节点
     * @return TRUE-满足发消息条件，FALSE-不满足发消息条件
     */
    public static Boolean checkTriggerFlowApprovalNode(TriggerSettingBo triggerSetting, List<FlowTaskNode> flowTaskNodes) {
        if(StringUtils.isBlank(triggerSetting.getCondition())) {
            return Boolean.FALSE;
        }
        if (CollectionUtils.isEmpty(flowTaskNodes)) {
            return Boolean.FALSE;
        }
        FlowApprovalNodeCondition condition = JSON.parseObject(triggerSetting.getCondition(), FlowApprovalNodeCondition.class);
        if (CollectionUtils.isEmpty(condition.getNodeIds())) {
            return Boolean.FALSE;
        }
        return flowTaskNodes.stream().anyMatch(taskNode -> condition.getNodeIds().contains(taskNode.getNodeId()));
    }
}
