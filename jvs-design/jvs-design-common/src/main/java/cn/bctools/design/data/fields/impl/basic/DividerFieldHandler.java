package cn.bctools.design.data.fields.impl.basic;

import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 表单字段: 分割线
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "分割线", type = DataFieldType.divider)
@AllArgsConstructor
public class DividerFieldHandler implements IDataFieldHandler<FieldBasicsHtml> {

}
