package cn.bctools.rule.dingding.work;

import cn.bctools.rule.annotations.SelectOptionField;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * The type Ding ms media id option.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
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
