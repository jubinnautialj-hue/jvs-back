package cn.bctools.design.data.fields.dto.form.html;

import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 计数器
 *
 * @author wl
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class InputNumberHtml extends FieldBasicsHtml {

    @ApiModelProperty("小数位数")
    private Integer precision;
    @ApiModelProperty("最大值")
    private Float max;
    @ApiModelProperty("最小值")
    private Float min;

    @ApiModelProperty("默认值")
    private Integer defaultValue;

}
