package cn.bctools.design.workflow.entity.dto;

import cn.bctools.design.workflow.model.properties.AutoApproval;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zhuxiaokang
 * 工作流设计扩展信息
 */

@Data
@ApiModel("工作流设计扩展信息")
public class FlowExtendDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("是否允许发起人终止流程")
    private Boolean enableCancel = Boolean.FALSE;

    @ApiModelProperty(value = "是否允许发起人重启任务", notes = "任务终止后，可重新启动该任务")
    private Boolean enableRestart = Boolean.FALSE;

    @ApiModelProperty("全局自动审批规则")
    private AutoApproval autoApproval;

    @ApiModelProperty(value = "是否允许动态选择审批人", notes = "发起流程时/审批，可自行选择后续节点的审批人")
    private Boolean enableDynamicApprover = Boolean.FALSE;

    @ApiModelProperty(value = "是否启用动态增加流程节点")
    private Boolean enableDynamicNode = Boolean.FALSE;

    @ApiModelProperty(value = "任务开始触发事件")
    private TaskStartTriggerEventDto taskStartTriggerEvent;

    @ApiModelProperty(value = "任务结束触发事件")
    private TaskEndTriggerEventDto taskEndTriggerEvent;

    @ApiModelProperty(value = "流程任务编号格式")
    private TaskCodeFormatDto codeFormat;

    @ApiModelProperty(value = "自定义流程任务标题格式")
    private String taskTitleFormat;

    @ApiModelProperty(value = "全局审批按钮配置")
    private CustomFlowButtonDto flowButton;

}
