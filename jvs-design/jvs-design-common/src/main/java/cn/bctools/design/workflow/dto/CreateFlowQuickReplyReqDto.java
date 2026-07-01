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
@ApiModel("创建快捷回复内容")
public class CreateFlowQuickReplyReqDto {

    @ApiModelProperty(value = "内容", required = true)
    @NotBlank(message = "快捷回复内容不能为空")
    private String content;
}
