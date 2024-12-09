package cn.bctools.design.data.fields.impl.container;

import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.html.StepHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 表单字段: 步骤条
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "步骤条", type = DataFieldType.step)
@AllArgsConstructor
public class StepFieldHandler implements IDataFieldHandler<StepHtml> {

}
