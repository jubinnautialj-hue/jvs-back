package cn.bctools.design.data.fields.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author jvs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@ApiModel("修改模型")
public class UpdateDataModelDto extends SaveDataModelDto {
    @ApiModelProperty(value = "模型id", required = true)
    @NotNull(message = "模型id不能为空")
    private String id;
}
