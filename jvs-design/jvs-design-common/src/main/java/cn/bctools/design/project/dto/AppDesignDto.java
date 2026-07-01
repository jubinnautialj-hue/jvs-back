package cn.bctools.design.project.dto;

import cn.bctools.design.data.fields.enums.DesignType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 应用类型
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
public class AppDesignDto implements Serializable {

    @NotBlank(message = "应用id不能为空")
    @ApiModelProperty(value = "应用id", required = true)
    private String appId;

    @NotBlank(message = "设计id不能为空")
    @ApiModelProperty(value = "设计id", required = true)
    private String designId;

    @NotNull(message = "设计类型不能为空")
    @ApiModelProperty(value = "设计类型", required = true)
    private DesignType designType;
    @ApiModelProperty(value = "移动端显示")
    private Boolean mobileDisplay;
    @ApiModelProperty(value = "pc端显示")
    private Boolean pcDisplay;
}
