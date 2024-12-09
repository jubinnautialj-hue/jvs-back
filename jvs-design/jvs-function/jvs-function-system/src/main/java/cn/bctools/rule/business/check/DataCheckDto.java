package cn.bctools.rule.business.check;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.hutool.core.util.DesensitizedUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class DataCheckDto {

    @ParameterValue(info = "校验数据", explain = "键值对数据", type = InputType.map)
    @NotNull(message = "数据不能为空")
    public Map<String, Object> body;
    @ParameterValue(info = "校验规则", explain = "参数名为校验数据中的参数名", type = InputType.mapValueSelected, cls = DataCheckSelected.class)
    public Map<String, String> check;

}
