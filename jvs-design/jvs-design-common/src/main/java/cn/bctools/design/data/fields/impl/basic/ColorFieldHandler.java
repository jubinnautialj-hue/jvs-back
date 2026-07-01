package cn.bctools.design.data.fields.impl.basic;

import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.html.ColorSelectHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 表单字段: 颜色选择
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "颜色选择", type = DataFieldType.colorSelect)
@AllArgsConstructor
public class ColorFieldHandler implements IDataFieldHandler<ColorSelectHtml> {

}
