package cn.bctools.design.project.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * 应用类型修改
 *
 * @Author: GuoZi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel("修改应用类型")
public class AppTypeUpdateDto extends AppTypeDto {

    @ApiModelProperty(value = "目录id", required = true)
    @NotBlank(message = "目录id不能为空")
    private String id;

    @NotBlank(message = "应用目录不能为空")
    @Length(max = 64, message = "应用目录最多64字")
    @ApiModelProperty(value = "新的应用目录", required = true)
    private String newType;

}
