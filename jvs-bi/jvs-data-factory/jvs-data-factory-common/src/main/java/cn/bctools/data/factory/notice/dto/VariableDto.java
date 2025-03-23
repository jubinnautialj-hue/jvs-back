package cn.bctools.data.factory.notice.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author: ZhuXiaoKang
 * @Description: 变量
 */
@Data
@Accessors(chain = true)
@ApiModel("变量")
public class VariableDto {
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("值")
    private String value;
}
