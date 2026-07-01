package cn.bctools.design.rule.impl.event;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class EventDto {

    @ParameterValue(info = "写入数据", explain = "请填写要写入的数据", type = InputType.input, defaultValue = "")
    public String content;
    @ParameterValue(info = "事件名称", explain = "事件名称", type = InputType.input, defaultValue = "message")
    public String name;

}
