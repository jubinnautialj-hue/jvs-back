package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class PhoneLocationDto {

    @NotNull(message = "手机号不能为空")
    @ParameterValue(type = InputType.input, info = "手机号", explain = "请输入11位手机号")
    public String phone;

}
