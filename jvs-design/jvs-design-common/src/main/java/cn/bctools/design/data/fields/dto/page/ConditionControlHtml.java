package cn.bctools.design.data.fields.dto.page;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class ConditionControlHtml {

    @ApiModelProperty(value = "背景颜色")
    private String bgcolor;

    @ApiModelProperty(value = "文字颜色")
    private String color;

    @ApiModelProperty(value = "字段名")
    private String key;

    @ApiModelProperty(value = "字段值")
    private String value;

    @ApiModelProperty(value = "字段值")
    private String text;

    @ApiModelProperty(value = "计划颜色")
    private String plainColor;

    @ApiModelProperty(value = "实际颜色")
    private String reallyColor;


}
