package cn.bctools.design.rule.selectoption;

import cn.bctools.rule.annotations.SelectOption;
import cn.bctools.rule.annotations.SelectOptionField;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author wl
 */
@Data
@Accessors(chain = true)
@SelectOption("企查查")
public class QiChaChaOption {

    @SelectOptionField("名称")
    public String name;
    @SelectOptionField("appkey")
    public String appkey;
    @SelectOptionField("secretKey")
    public String secretKey;

}
