package cn.bctools.rule.ess;

import cn.bctools.rule.annotations.SelectOption;
import cn.bctools.rule.annotations.SelectOptionField;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author jvs
 * The type Tencen cloud api option.
 */
@Data
@Accessors(chain = true)
@SelectOption("腾讯电子签")
public class TencenCloudApiOption {

    /**
     * The Name.
     */
    @SelectOptionField("名称")
    public String name;
    /**
     * The Secret id.
     */
    @SelectOptionField("secretId")
    public String secretId;
    /**
     * The Secret key.
     */
    @SelectOptionField("secretKey")
    public String secretKey;
    /**
     * The End point.
     */
    @SelectOptionField("endPoint")
    public String endPoint;
    ;


}
