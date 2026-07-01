package cn.bctools.design.workflow.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author jvs
 */
@Data
@ApiModel("任务开始触发事件")
public class TaskStartTriggerEventDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("启动流程前触发事件")
    private TaskTriggerEventSetting beforeStartFlowEvent;
}
