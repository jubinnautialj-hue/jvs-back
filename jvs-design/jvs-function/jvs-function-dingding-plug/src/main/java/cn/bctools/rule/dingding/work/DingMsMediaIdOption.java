package cn.bctools.rule.dingding.work;

import cn.bctools.rule.annotations.SelectOption;
import cn.bctools.rule.annotations.SelectOptionField;
import cn.bctools.rule.constant.RuleConstant;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * The type Ding ms media id option.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
@SelectOption(RuleConstant.DINGDINGRESOURCE_OPTION)
public class DingMsMediaIdOption {

    /**
     * The Name.
     */
    @SelectOptionField("名称")
    public String name;

    /**
     * The Media id.
     */
    @SelectOptionField("资源id")
    public String mediaId;

}
