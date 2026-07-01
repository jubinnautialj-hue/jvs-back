package cn.bctools.design.notice.handler.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 消息触发类型枚举
 */
@Getter
@AllArgsConstructor
public enum TriggerTypeEnum {
    // 数据操作
    CREATED("created", "创建成功"),
    EDITED("edited", "编辑成功"),
    DELETED("deleted", "删除成功"),

    // 流程
    FLOW_CREATED("flow_created", "启动流程"),
    FLOW_APPROVAL_RESULTS("flow_approval_results", "审批结果"),
    FLOW_APPROVAL_NODE("flow_approval_node", "审批节点"),

    FLOW_REMIND("FLOW_REMIND", "工作流待办提醒"),
    FLOW_URGE("FLOW_URGE", "催办"),


    ;

    @JsonValue
    private final String value;
    private final String desc;
}
