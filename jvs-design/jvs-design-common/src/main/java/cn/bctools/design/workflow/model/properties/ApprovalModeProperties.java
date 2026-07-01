package cn.bctools.design.workflow.model.properties;

import cn.bctools.design.workflow.model.enums.CountersignRuleEnum;
import lombok.Data;

/**
 * @author zhuxiaokang
 * 审批模式配置
 */
@Data
public class ApprovalModeProperties {

    /**
     * 会签规则
     */
    private CountersignRuleEnum countersignRule;

    /**
     * 值
     */
    private long value;

    /**
     * 满足条件后，是否立即结束当前节点审批
     * true-当前节点未审批人员不再审批，立即流转到下一个节点(默认)
     * false-当前节点未审批人员可继续审批
     */
    private Boolean endNow = Boolean.TRUE;
}