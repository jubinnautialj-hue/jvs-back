package cn.bctools.function.utils;

import cn.bctools.function.enums.JvsParamType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 表达式参数
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@ApiModel("表达式参数")
public class ExpressionParam {

    @ApiModelProperty("是否为运算符")
    private boolean isOperator;

    @ApiModelProperty("参数值")
    private Object value;

    @ApiModelProperty("参数值类型")
    private JvsParamType valueType;

    @ApiModelProperty("函数名称")
    private String functionName;

    @ApiModelProperty("参数数量")
    private int paramCount;

}
