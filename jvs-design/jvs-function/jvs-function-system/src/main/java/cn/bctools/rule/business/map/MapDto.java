package cn.bctools.rule.business.map;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.dto.BindDto;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author st
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class MapDto extends BindDto {

    @ParameterValue(info = "内容", type = InputType.map)
    @NotNull(message = "内容不能为空")
    public Map<String, Object> content;


}
