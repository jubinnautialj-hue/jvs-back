package cn.bctools.design.project.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author zhuxiaokang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel("提交测试版本入参")
public class AppVersionSubmitBetaDto extends VersionIterationBaseDto {

    @ApiModelProperty(value = "应用版本号", required = true)
    @NotBlank(message = "请指定版本号")
    @Size(max = 60, message = "版本号不能超过60个字符")
    private String appVersion;

    @ApiModelProperty("备注")
    @Size(max = 240, message = "备注不能超过240个字符")
    private String description;
}
