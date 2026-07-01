package cn.bctools.design.data.fields.dto.form.html;

import cn.bctools.design.data.fields.dto.form.MultipleHtml;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wl
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "多选")
public class CheckboxHtml extends MultipleHtml {
}
