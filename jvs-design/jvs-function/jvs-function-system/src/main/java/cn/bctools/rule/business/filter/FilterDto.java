package cn.bctools.rule.business.filter;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.dto.QueryConditionDto;
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
public class FilterDto {
    @ParameterValue(info = "内容", explain = "数组对象", type = InputType.listMap)
    @NotNull(message = "内容不能为空")
    public List<Object> body;

    @ParameterValue(info = "查询类型", explain = "查询类型 默认为且", type = InputType.selected, cls = FilterTypeSelected.class)
    @NotNull(message = "类型不能为空")
    public String type;

    @NotNull(message = "查询条件不能为空")
    @ParameterValue(info = "过滤条件", explain = "多个条件，按查询类型进行过滤", type = InputType.filterMap)
    public List<QueryConditionDto> filter;

}
