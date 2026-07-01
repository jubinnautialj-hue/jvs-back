package cn.bctools.design.data.fields.dto.form;

import cn.bctools.design.data.fields.dto.form.item.SelectItemHtml;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class MultipleHtml extends SelectItemHtml {

    @ApiModelProperty("可否多选")
    private Boolean multiple;
    @ApiModelProperty("是否选择路径")
    private Boolean showalllevels;
}
