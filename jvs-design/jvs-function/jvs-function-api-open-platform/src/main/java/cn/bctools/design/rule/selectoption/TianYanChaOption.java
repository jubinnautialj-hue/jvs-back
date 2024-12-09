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
@SelectOption("天眼查")
public class TianYanChaOption {

    @SelectOptionField("名称")
    public String name;
    @SelectOptionField("token")
    public String token;

}
