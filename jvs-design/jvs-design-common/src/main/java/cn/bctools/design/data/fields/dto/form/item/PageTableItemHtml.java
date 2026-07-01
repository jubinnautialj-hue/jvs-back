package cn.bctools.design.data.fields.dto.form.item;

import cn.bctools.design.data.fields.dto.form.MultipleHtml;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "内嵌列表页")
@EqualsAndHashCode(callSuper = true)
public class PageTableItemHtml extends MultipleHtml {
}
