package cn.bctools.rule.tools.irs;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class IRSInvoiceModelDto {
    @ParameterValue(info = "私钥", type = InputType.input)
    public String privateKeyStr;
    @ParameterValue(info = "SM4key", type = InputType.input)
    public String sm4Key;
    @ParameterValue(info = "SM4Iv", type = InputType.input)
    public String sm4IV;
    @ParameterValue(info = "纳税人识别号", type = InputType.input)
    public String NSRSBH;
    @ParameterValue(info = "授权编号", type = InputType.input)
    public String SQBH;
    @ParameterValue(info = "授权文件地址", type = InputType.input)
    public String SQWJ;
    @ParameterValue(info = "授权文件名称", type = InputType.input)
    public String SQWJMC;
    @ParameterValue(info = "授权日期起", type = InputType.input)
    public String SQRRQ;
    @ParameterValue(info = "授权日期止", type = InputType.input)
    public String SQRRZ;
    @ParameterValue(info = "纳税人名称", type = InputType.input)
    public String NSRMC;
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
    @ParameterValue(info = "当前服务版本号，默认传1.0", type = InputType.input)
    public String version;
}
