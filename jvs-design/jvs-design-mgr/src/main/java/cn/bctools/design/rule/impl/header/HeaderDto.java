package cn.bctools.design.rule.impl.header;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class HeaderDto {

    @ParameterValue(info = "请求key", explain = "示例：Host、User-Agent、Referer、Cookie等信息", type = InputType.input)
    @NotNull(message = "请求key不能为空")
    public String content;

}
