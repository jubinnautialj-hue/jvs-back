package cn.bctools.design.data.fields.dto.form.html;

import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.FieldPublicHtml;
import cn.bctools.design.data.fields.impl.basic.DatePickerFieldHandler;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "日期")
@Accessors(chain = true)
public class DatePickerHtml extends FieldBasicsHtml {
    /**
     * 类型
     */
    DatePickerFieldHandler.DateType datetype;
}
