package cn.bctools.design.workflow.model.properties;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhuxiaokang
 * 人工审批节点自动审批规则
 */
@Data
public class AutoApproval implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 发起人自动审批: 审批人为发起人时，自动审批
     * TRUE-自动审批，FALSE-不自动审批
     */
    private Boolean selfAuto;

    /**
     * 相邻节点自动审批：当前节点审批人在上一节点有审批结果时，自动审批
     * TRUE-自动审批，FALSE-不自动审批
     */
    private Boolean adjacentNode;
}
