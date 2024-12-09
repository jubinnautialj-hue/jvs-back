package cn.bctools.rule.tools.random;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class RandomRangeDto  {

    @ParameterValue(info = "最小值(包含)", type = InputType.number, defaultValue = "0")
    public Long min = 0L;

    @ParameterValue(info = "最大值(不包含)", type = InputType.number, defaultValue = "100")
    public Long max = 100L;

    @ParameterValue(info = "变量绑定", explain = "变量数据绑定,用于修改数据值", type = InputType.bind)
    public List<Map> bind;
}
