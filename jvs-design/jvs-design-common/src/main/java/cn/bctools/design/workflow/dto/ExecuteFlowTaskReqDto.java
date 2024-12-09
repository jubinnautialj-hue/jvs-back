package cn.bctools.design.workflow.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("工作流处理操作信息入参")
public class ExecuteFlowTaskReqDto {

    @ApiModelProperty(value = "工作流任务id", required = true)
    @NotBlank(message = "工作流任务id不能为空")
    private String taskId;

    @ApiModelProperty(value = "工作流任务节点id", required = true)
    @NotBlank(message = "工作流任务节点id不能为空")
    private String nodeId;

    @ApiModelProperty(value = "内容JSON")
    private Map<String, Object> data;
}
