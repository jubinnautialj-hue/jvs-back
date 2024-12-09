package cn.bctools.design.rule.impl.dingding.dingding;

import cn.bctools.rule.annotations.SelectOption;
import cn.bctools.rule.annotations.SelectOptionField;
import cn.bctools.rule.constant.RuleConstant;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
@SelectOption(RuleConstant.DINGDING_OPTION)
public class DingDingSelectedOption {

    @SelectOptionField("名称")
    public String name;

    @SelectOptionField("webhook地址")
    public String webhook;

    @SelectOptionField("secret凭证")
    public String secret;

}
