package cn.bctools.rule.business.listmap;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author st
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ListMapDto {

    @ParameterValue(info = "内容", type = InputType.listMap, subtype = InputType.longtext)
    @NotNull(message = "内容不能为空")
    public List<Object> content;


}
