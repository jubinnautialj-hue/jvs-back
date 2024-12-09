package cn.bctools.rule.data.mq;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author st
 */
@Accessors(chain = true)
@Data
public class RabbitMqDto {


    @ParameterValue(info = "操作类型", type = InputType.selected,   cls = RabbitMqSelected.class)
    @NotNull(message = "请求类型不能为空")
    public String type;

    @ParameterValue(info = "发送的内容", type = InputType.json)
    @NotNull(message = "发送的内容不能为空")
    public String body;

}
