package cn.bctools.rule.tools.text;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author My_gj
 */
@Accessors(chain = true)
@Data
public class TextFunctionDto {
    @ParameterValue(info = "内容", type = InputType.html)
    @NotNull(message = "内容不能为空")
    public String content;
    @ParameterValue(info = "是否去掉标签", type = InputType.onOff)
    public Boolean clear;
    @ParameterValue(info = "内容替换值", explain = "内容中如果存在变量,则自动替换,变量示例:${变量名}", type = InputType.map, necessity = false)
    public Map<String, String> text;
}
