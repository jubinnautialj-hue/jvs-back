package cn.bctools.rule.risk;

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
public class RiskDto {

    @ParameterValue(info = "规则名称", type = InputType.selected, cls = RiskParameterSelected.class)
    @NotNull(message = "规则名称不能为空")
    public String riskName;

    @ParameterValue(info = "body请求参数,根据规则需要的参数进行填写", necessity = false, type = InputType.map)
    public Map<String, Object> body;

}

