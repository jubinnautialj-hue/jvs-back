package cn.bctools.design.project.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 应用类型
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@ApiModel("应用类型")
public class AppTypeDto implements Serializable {

    @NotBlank(message = "应用id不能为空")
    @ApiModelProperty(value = "应用id", required = true)
    private String appId;

    @NotBlank(message = "应用目录不能为空")
    @Length(max = 64, message = "应用目录最多64字")
    @ApiModelProperty(value = "应用目录", required = true)
    private String type;

    @ApiModelProperty(value = "图标字段")
    private String icon;

    @ApiModelProperty(value = "上级目录id")
    private String parentId;

}
