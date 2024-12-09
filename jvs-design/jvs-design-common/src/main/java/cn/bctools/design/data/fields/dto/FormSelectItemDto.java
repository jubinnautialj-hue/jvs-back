package cn.bctools.design.data.fields.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 表单的下拉框选项
 *
 * @Author: GuoZi
 */
@Data
@ApiModel("表单的下拉框选项")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FormSelectItemDto {

    @ApiModelProperty("表单唯一标识")
    private String id;

    @ApiModelProperty("表单名称")
    private String name;

    @ApiModelProperty("数据模型id")
    private String dataModelId;

}
