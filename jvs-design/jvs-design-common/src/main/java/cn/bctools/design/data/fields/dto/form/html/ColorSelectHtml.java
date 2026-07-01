package cn.bctools.design.data.fields.dto.form.html;

import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wl
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "颜色选择")
public class ColorSelectHtml extends FieldBasicsHtml {
}

