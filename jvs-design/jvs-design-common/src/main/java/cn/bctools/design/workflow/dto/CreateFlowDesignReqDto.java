package cn.bctools.design.workflow.dto;

import cn.bctools.design.workflow.entity.dto.FlowExtendDto;
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
@ApiModel("创建工作流入参")
public class CreateFlowDesignReqDto {

    @ApiModelProperty(value = "应用id", required = true)
    @NotBlank(message = "应用id不能为空")
    private String jvsAppId;

    @ApiModelProperty(value = "流程名称")
    @NotBlank(message = "流程名称不能为空")
    private String name = "未命名流程";

    @ApiModelProperty(value = "分类")
    private String designGroup = "未分类";

    @ApiModelProperty(value = "图标")
    private String icon;

    @ApiModelProperty(value = "扩展配置")
    private FlowExtendDto extend;

    @ApiModelProperty(value = "描述")
    private String description;


}
