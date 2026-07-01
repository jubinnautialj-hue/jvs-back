package cn.bctools.design.template.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 由模板创建
 *
 * @Author: GuoZi
 */
@Data
@ApiModel("由模板创建")
public class TemplateCreateDto {

    @NotBlank(message = "模板id不能为空")
    @ApiModelProperty("模板id")
    private String id;

    @NotBlank(message = "应用id不能为空")
    @ApiModelProperty("应用id")
    private String appId;

}
