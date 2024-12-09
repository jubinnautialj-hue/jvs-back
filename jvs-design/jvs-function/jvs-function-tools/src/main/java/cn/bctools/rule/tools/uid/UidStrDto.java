package cn.bctools.rule.tools.uid;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author guojing
 */
@Accessors(chain = true)
@Data
public class UidStrDto {

    @ParameterValue(info = "显示进制位数,最大36", explain = "默认10进行", type = InputType.number, defaultValue = "10")
    public Integer radix;

}
