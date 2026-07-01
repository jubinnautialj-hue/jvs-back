package cn.bctools.design.workflow.dto.testflow;

import cn.bctools.design.workflow.dto.startflow.StartFlowReqDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * @author zhuxiaokang
 * 发起工作流测试任务入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("发起工作流测试任务入参")
public class StartFlowTestDto extends StartFlowReqDto {

    @ApiModelProperty(value = "发起测试的用户id", required = true)
    @NotBlank(message = "请选择用户")
    private String userId;

}
