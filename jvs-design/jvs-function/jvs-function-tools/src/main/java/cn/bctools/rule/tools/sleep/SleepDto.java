package cn.bctools.rule.tools.sleep;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jvs
 */
@Accessors(chain = true)
@Data
public class SleepDto {

    @ParameterValue(info = "休眠时间(秒,默认5秒)", defaultValue = "5", type = InputType.number)
    public Integer sleep;

}
