package cn.bctools.rule.bctools;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;

/**
 * @author Administrator
 */
@Accessors(chain = true)
@Data
public class JvsFunctionDto {

    @NotEmpty(message = "加解密数据不能为空")
    @ParameterValue(info = "数据", explain = "只支持字符串", necessity = false, type = InputType.longtext)
    public String body;
    @ParameterValue(info = "加密或解密", type = InputType.onOff, defaultValue = "false")
    public Boolean onOff = false;

}
