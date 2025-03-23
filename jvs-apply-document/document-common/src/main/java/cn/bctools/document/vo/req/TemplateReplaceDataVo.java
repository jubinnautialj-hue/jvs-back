package cn.bctools.document.vo.req;


import cn.bctools.document.enums.VariableTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @Author: admin
 * @Description: 模板变量替换入参
 */

@Data
@Accessors(chain = true)
@ApiModel("模板变量替换 变量参数")
public class TemplateReplaceDataVo {
    @ApiModelProperty(value = "变量名称")
    @NotNull(message = "变量名称不能为空")
    private String name;
    @ApiModelProperty(value = "变量值")
    @NotNull(message = "变量值不能为空")
    private String value;
    @ApiModelProperty(value = "变量类型")
    @NotNull(message = "变量值不能为空")
    private VariableTypeEnum variableType;
    @ApiModelProperty(value = "其他参数，例如:图片后缀名，png,jpeg")
    private String other;

}
