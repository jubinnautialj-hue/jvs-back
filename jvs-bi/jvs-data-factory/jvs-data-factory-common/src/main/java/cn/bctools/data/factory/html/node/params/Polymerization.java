package cn.bctools.data.factory.html.node.params;

import cn.bctools.data.factory.enums.CollectTypeEnum;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "统计入参")
public class Polymerization {
    @ApiModelProperty(value = "字段名")
    String fieldKey;
    @ApiModelProperty(value = "显示名称")
    String fieldName;
    @ApiModelProperty("格式类型例如时间类型:yyyy-MM-dd yyyy/MM/dd")
    private String format;
    @ApiModelProperty("字段类型")
    private DataFieldTypeEnum fieldType;
    @ApiModelProperty(value = "类型")
    CollectTypeEnum groupMethodVal;
    @ApiModelProperty(value = "计算时的小数保留位数默认为2")
    private Integer decimalPlace;
    @ApiModelProperty(value = "是否为截断")
    private Boolean truncation;
}
