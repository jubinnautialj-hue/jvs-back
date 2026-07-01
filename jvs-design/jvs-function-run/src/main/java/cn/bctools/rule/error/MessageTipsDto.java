package cn.bctools.rule.error;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.dto.BindDto;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author guojing 异常处理节点
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class MessageTipsDto extends BindDto {

    @ParameterValue(info = "提示消息", explain = "用户右下角接受到的消息提示", type = InputType.longtext)
    public String message;
    @ParameterValue(info = "消息样式", explain = "支持成功和失败两种展示", type = InputType.selected, cls = MessageTipsSelected.class)
    public Boolean onOff = true;
    @ParameterValue(info = "自动关闭", type = InputType.selected, cls = MessageCloseSelected.class)
    public Boolean off = true;
    @ParameterValue(info = "返回的数据", type = InputType.map, necessity = false)
    public Object data;

}
