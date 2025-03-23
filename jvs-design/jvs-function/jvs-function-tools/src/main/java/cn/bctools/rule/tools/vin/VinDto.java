package cn.bctools.rule.tools.vin;

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
public class VinDto {

    @ParameterValue(info = "vin号前8位", type = InputType.input)
    public String vinTopEight;
    @ParameterValue(info = "1位年份位", type = InputType.input)
    public String yearLetter;
    @ParameterValue(info = "1位工厂代码", type = InputType.input)
    public String factoryCode;
    @ParameterValue(info = "1位装配线代码", type = InputType.input)
    public String assemblyLineCode;
    @ParameterValue(info = "5位流水号", type = InputType.input)
    public String serialNumber;

//    @ParameterValue(info = "17位vin,校验vin号", type = InputType.input)
//    public String vin;


}
