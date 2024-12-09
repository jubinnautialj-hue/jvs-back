package cn.bctools.design.workflow.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 回退范围
 */
@Getter
@AllArgsConstructor
public enum BackScopeEnum {

    /**
     * 已审批节点
     * 回退时，回显所有已审批的节点，让审批人选择回退节点
     */
    APPROVED,
    /**
     * 上一节点
     * 回退到上一个审批节点
     */
    PREVIOUS,
    /**
     * 发起人节点
     * 回退到发起人节点
     */
    ROOT,
    ;

}
