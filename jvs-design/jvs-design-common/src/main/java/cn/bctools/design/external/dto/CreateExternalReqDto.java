package cn.bctools.design.external.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * @author zhuxiaokang
 * 创建外部页面配置入参
 */
@Data
@Accessors(chain = true)
@ApiModel("创建外部页面配置入参")
public class CreateExternalReqDto {

    @ApiModelProperty(value = "页面名", required = true)
    @NotBlank(message = "页面名不能为空")
    private String name;

    @ApiModelProperty(value = "外部页面地址", required = true)
    @NotBlank(message = "页面地址不能为空")
    private String url;
}
