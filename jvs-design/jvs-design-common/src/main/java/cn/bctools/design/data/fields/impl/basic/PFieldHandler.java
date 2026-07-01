package cn.bctools.design.data.fields.impl.basic;

import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.item.BaseItemHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 表单字段: 小标题
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "小标题", type = DataFieldType.p)
@AllArgsConstructor
public class PFieldHandler implements IDataFieldHandler<BaseItemHtml> {

}
