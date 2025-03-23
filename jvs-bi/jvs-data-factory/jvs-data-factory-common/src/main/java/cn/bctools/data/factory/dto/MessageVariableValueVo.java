package cn.bctools.data.factory.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("权限标识")
public class MessageVariableValueVo {
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("值")
    private String value;
    @ApiModelProperty("显示颜色")
    private String color;
}
