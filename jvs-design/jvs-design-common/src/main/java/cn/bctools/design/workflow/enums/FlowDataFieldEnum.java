package cn.bctools.design.workflow.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 工作流在数据模型中的字段
 * 不是所有字段都需要保存到模型中，部分字段只在查询时填充到填充到数据中
 */
@Getter
@AllArgsConstructor
public enum FlowDataFieldEnum {
    /**
     * 任务的当前执行状态
     */
    TASK_STATE("jvsFlowTaskState", "工作流状态"),
    /**
     * 工作流进度
     */
    TASK_PROGRESS("jvsFlowTaskProgress", "工作流进度"),
    /**
     * 工作流任务
     */
    TASK("jvsFlowTask", "工作流任务"),
    /**
     * 存储数据的所有工作流任务信息
     */
    TASK_HISTORY("jvsFlowTaskHistory", "历史工作流任务"),
    /**
     * 逻辑key
     */
    RULE_KEY("ruleKey", "逻辑key"),
    /**
     * false-非测试数据，true-测试数据
     */
    JVS_MODEL_TEST("jvsModelTest", "是否是测试数据"),
    /**
     * 流程参与者id集合
     */
    TASK_PERSON_IDS("jvsTaskPersonIds", "流程参与者id集合"),
    ;

    private String fieldKey;
    private String fieldName;
}
