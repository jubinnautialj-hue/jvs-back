package cn.bctools.rule.idcard;

import cn.bctools.rule.annotations.SelectOption;
import cn.bctools.rule.annotations.SelectOptionField;
import cn.bctools.rule.constant.RuleConstant;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
@SelectOption(RuleConstant.ALICODE_OPTION)
public class AliCodeOption {

    @SelectOptionField("名称")
    public String name;
    @SelectOptionField(value = "唯一码", type = InputType.password)
    public String code;
}
