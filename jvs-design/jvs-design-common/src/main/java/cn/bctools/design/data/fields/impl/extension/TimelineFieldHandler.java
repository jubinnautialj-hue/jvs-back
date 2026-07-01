package cn.bctools.design.data.fields.impl.extension;

import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.html.TimeLineHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 表单字段: 时间线
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "时间线", type = DataFieldType.timeline)
@AllArgsConstructor
public class TimelineFieldHandler implements IDataFieldHandler<TimeLineHtml> {

}
