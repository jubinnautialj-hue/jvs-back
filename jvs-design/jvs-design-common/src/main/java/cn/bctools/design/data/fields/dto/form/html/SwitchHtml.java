package cn.bctools.design.data.fields.dto.form.html;

import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wl
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "开关")
public class SwitchHtml extends FieldBasicsHtml {
    @ApiModelProperty("打开时的文字描述")
    private String activetext;
    @ApiModelProperty("关闭时的文字描述")
    private String inactivetext;
}

