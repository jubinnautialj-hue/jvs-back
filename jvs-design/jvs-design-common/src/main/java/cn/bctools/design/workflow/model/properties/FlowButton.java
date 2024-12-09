package cn.bctools.design.workflow.model.properties;

import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import lombok.Data;

/**
 * @author zhuxiaokang
 * 工作流按钮
 */
@Data
public class FlowButton {

    /**
     * 按钮名称
     */
    private String name;

    /**
     * 操作类型
     */
    private NodeOperationTypeEnum operation;

    /**
     * 是否启用 true-启用，false-禁用
     */
    private Boolean enable;

    /**
     * 集成&自动化节点属性配置
     */
    private AutomationProperties automation;

    /**
     * 流程回退配置
     */
    private BackProperties back;
}
