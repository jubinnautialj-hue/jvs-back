package cn.bctools.rule.tools.irs;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class IRSgetInvoiceModelDto {

    @ParameterValue(info = "SM4key", type = InputType.input)
    public String sm4Key;
    @ParameterValue(info = "SM4Iv", type = InputType.input)
    public String sm4IV;
    @ParameterValue(info = "私钥", type = InputType.input)
    public String privateKeyStr;
    @ParameterValue(info = "纳税人识别号", type = InputType.input)
    public String NSRSBH;
    @ParameterValue(info = "调用服务名", type = InputType.input)
    public String tranId;
    @ParameterValue(info = "渠道代码", type = InputType.input)
    public String channelId;
    @ParameterValue(info = "32位uud不可重复", type = InputType.input)
    public String tranSeq;
    @ParameterValue(info = "当前日期格式yyyyMMdd如：20220101", type = InputType.input)
    public String tranDate;
    @ParameterValue(info = "系统时间格式hhmmssSSS如161616001", type = InputType.input)
    public String tranTime;
    @ParameterValue(info = "前服务版本号，默认传1.0", type = InputType.input)
    public String version;
}
