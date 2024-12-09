package cn.bctools.design.workflow.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("快捷创建工作流入参")
public class QuickCreationFlowDesignReqDto {

    @ApiModelProperty(value = "应用id", required = true)
    @NotBlank(message = "应用id不能为空")
    private String jvsAppId;

    @ApiModelProperty(value = "数据模型id", required = true)
    @NotBlank(message = "数据模型id不能为空")
    private String dataModelId;

}
