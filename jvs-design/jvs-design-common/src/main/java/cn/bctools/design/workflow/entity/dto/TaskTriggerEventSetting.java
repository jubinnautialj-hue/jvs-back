package cn.bctools.design.workflow.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zhuxiaokang
 */
@Data
@ApiModel("任务结束触发事件设置")
public class TaskTriggerEventSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("TRUE-启用事件,FALSE-不启用事件")
    private Boolean enableEvent;
    @ApiModelProperty("逻辑设计key")
    private String ruleKey;
}
