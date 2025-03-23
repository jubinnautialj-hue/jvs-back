package cn.bctools.design.data.fields.dto.form.html;

import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
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
    private String prop;
    private String label;
    private int span;
    private String id;
    private boolean display;
    private String status;
    private String placeholder;
    private boolean clearable;
    private boolean disabled;
    private String prefixicon;
    private String startplaceholder;
    private String endplaceholder;
    private String rangeseparator;
    private String startLimit;
    private String endLimit;
    private String defaultValue;
    private String sqlType;
    private String linkbind;
    private String name;
    private String defaultOrigin;
    private boolean defaultDate;
}
