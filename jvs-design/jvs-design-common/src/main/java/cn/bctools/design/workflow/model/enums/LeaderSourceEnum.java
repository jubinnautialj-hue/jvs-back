package cn.bctools.design.workflow.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author jvs
 * 主管来源类型
 */
@Getter
@AllArgsConstructor
public enum LeaderSourceEnum {

    /**
     * 发起人
     */
    SEND_USER,
    /**
     * 流程节点
     */
    FLOW_NODE,
    /**
     * 成员字段
     */
    USER_FIELD,
    ;
}
