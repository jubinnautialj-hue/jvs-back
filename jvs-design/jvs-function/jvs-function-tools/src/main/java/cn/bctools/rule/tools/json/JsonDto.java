package cn.bctools.rule.tools.json;

import cn.bctools.rule.annotations.Inspect;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.dto.BindDto;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author st
 */
@Inspect
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class JsonDto extends BindDto {
    @ParameterValue(info = "JSON", type = InputType.json)
    @NotNull(message = "json不能为空")
    public String content;

}
