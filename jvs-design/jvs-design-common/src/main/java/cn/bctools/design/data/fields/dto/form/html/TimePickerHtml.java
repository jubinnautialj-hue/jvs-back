package cn.bctools.design.data.fields.dto.form.html;

import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.FieldPublicHtml;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 任意时间
 *
 * @author wl
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TimePickerHtml extends FieldBasicsHtml {
    /**
     * 范围选择
     */
    Boolean isrange;
}
