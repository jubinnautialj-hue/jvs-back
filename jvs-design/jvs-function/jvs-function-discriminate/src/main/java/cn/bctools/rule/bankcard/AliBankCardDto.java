package cn.bctools.rule.bankcard;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.idcard.AliCodeSelected;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class AliBankCardDto {

    @ParameterValue(info = "阿里APPcode", explain = "阿里APPcode", type = InputType.selected, cls = AliCodeSelected.class)
    public String appcode;
    @ParameterValue(info = "名字", explain = "名字", type = InputType.input)
    public String name;
    @ParameterValue(info = "身份证", explain = "身份证", type = InputType.input)
    public String idcard;
    @ParameterValue(info = "银行卡号", explain = "银行卡号", type = InputType.input)
    public String acctNo;

}

