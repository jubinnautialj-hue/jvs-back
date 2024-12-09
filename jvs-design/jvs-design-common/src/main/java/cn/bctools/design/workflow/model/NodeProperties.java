package cn.bctools.design.workflow.model;

import cn.bctools.design.workflow.model.enums.NodePropertiesModeEnum;
import cn.bctools.design.workflow.model.enums.NodePropertiesTypeEnum;
import cn.bctools.design.workflow.model.enums.NodePropertiesUserEmptyEnum;
import cn.bctools.design.workflow.model.properties.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang
 * 节点配置
 */
@Data
@Accessors(chain = true)
public class NodeProperties {
    /**
     * 开始节点属性
     */
    @ApiModelProperty(value = "权限")
    private List<Purview> purviews;

    /**
     * 审批节点属性
     */
    @ApiModelProperty(value = "审批模式")
    private NodePropertiesModeEnum mode;
    @ApiModelProperty(value = "审批模式配置")
    private ApprovalModeProperties modeProps;
    @ApiModelProperty(value = "审批人为空时处理")
    private NodePropertiesUserEmptyEnum userEmpty;
    @ApiModelProperty(value = "审批期限")
    private TimeLimit timeLimit;
    @ApiModelProperty(value = "主管信息")
    private Leader leader;
    @ApiModelProperty(value = "操作按钮配置")
    private List<FlowButton> btn;
    @ApiModelProperty(value = "加签属性配置")
    private AppendApprovalProperties appendApproval;
    @ApiModelProperty(value = "自动审批")
    private AutoApproval autoApproval;
    @ApiModelProperty(value = "人员选择范围")
    private PersonnelScope personnelScope;

    /**
     * 审批节点/抄送节点属性
     */
    @ApiModelProperty(value = "操作人员类型")
    private NodePropertiesTypeEnum type;
    @ApiModelProperty(value = "处理人/角色等信息")
    private Target targetObj;

    /**
     * 集成&自动化节点属性
     */
    private AutomationProperties automation;
}
