package cn.bctools.design.workflow.dto.manage;

import cn.bctools.design.workflow.dto.startflow.SelfSelectApprover;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author zxk
 */
@Data
@Accessors(chain = true)
@ApiModel("工作流任务——增员审批入参")
public class TaskAssignDto {

    @ApiModelProperty(value = "任务id", required = true)
    @NotBlank(message = "未指定任务id")
    private String taskId;

    @ApiModelProperty(value = "指派审批人", required = true)
    @NotEmpty(message = "未指定审批人")
    private List<SelfSelectApprover> approvers;
}
