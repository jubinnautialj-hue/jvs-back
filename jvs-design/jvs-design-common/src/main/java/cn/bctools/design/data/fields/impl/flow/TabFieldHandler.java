package cn.bctools.design.data.fields.impl.flow;

import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.item.BaseItemHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author guojing
 */
@Slf4j
@Component
@DesignField(value = "流程设计", type = DataFieldType.flowTable)
public class TabFieldHandler implements IDataFieldHandler<BaseItemHtml> {

}
