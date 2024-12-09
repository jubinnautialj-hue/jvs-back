package cn.bctools.design.workflow.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * @author zhuxiaokang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel("修改工作流入参")
public class UpdateFlowDesignReqDto extends CreateFlowDesignReqDto {

    @ApiModelProperty(value = "工作流id", required = true)
    @NotBlank(message = "工作流id不能为空")
    private String id;
}
