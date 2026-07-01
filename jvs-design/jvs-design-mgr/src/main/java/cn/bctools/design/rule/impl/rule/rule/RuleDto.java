package cn.bctools.design.rule.impl.rule.rule;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class RuleDto {

    @ParameterValue(info = "逻辑引擎远程调用key", type = InputType.input, explain = "逻辑引擎远程调用key")
    @NotNull(message = "逻辑引擎远程调用key不能为空")
    public String key;
    @ParameterValue(info = "请求业务数据", type = InputType.map, explain = "调用逻辑时需要调整的入参", necessity = false)
    public Map<String, Object> map;
}
