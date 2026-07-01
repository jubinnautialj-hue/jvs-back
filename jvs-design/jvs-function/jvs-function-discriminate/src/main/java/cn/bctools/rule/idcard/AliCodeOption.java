package cn.bctools.rule.idcard;

import cn.bctools.rule.annotations.SelectOptionField;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class AliCodeOption {

    @SelectOptionField(value = "唯一码", type = InputType.password)
    public String code;

}
