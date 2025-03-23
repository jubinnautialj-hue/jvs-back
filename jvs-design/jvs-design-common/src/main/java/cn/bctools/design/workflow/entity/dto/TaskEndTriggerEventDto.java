package cn.bctools.design.workflow.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zhuxiaokang
 */
@Data
@ApiModel("任务结束触发事件")
public class TaskEndTriggerEventDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("流程任务撤回触发事件")
    private TaskTriggerEventSetting terminatedEvent;
    @ApiModelProperty("流程任务审批通过触发事件")
    private TaskTriggerEventSetting passedEvent;
    @ApiModelProperty("流程任务审批不通过触发事件")
    private TaskTriggerEventSetting rejectedEvent;
}
