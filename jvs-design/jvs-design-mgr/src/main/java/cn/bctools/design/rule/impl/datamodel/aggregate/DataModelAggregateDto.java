package cn.bctools.design.rule.impl.datamodel.aggregate;


import cn.bctools.design.data.fields.dto.QueryConditionDto;
import cn.bctools.design.data.fields.enums.AggregateEnumType;
import cn.bctools.design.rule.impl.datamodel.DataModelLinkSelected;
import cn.bctools.design.rule.impl.datamodel.DataModelSelected;
import cn.bctools.rule.annotations.Inspect;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.selected.BooleanSelected;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author gx
 * 将模型数据转换为显示数据
 */
@Data
@Inspect
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class DataModelAggregateDto {

    @NotNull(message = "数据模型不能为空")
    @ParameterValue(info = "数据模型", explain = "选择当前应用下的模型", type = InputType.selected, cls = DataModelSelected.class, linkFields = {"fields", "body", "groupBy"}, linkCls = DataModelLinkSelected.class)
    public String dataModelId;

    @NotNull(message = "查询条件不能为空")
    @ParameterValue(info = "查询条件", type = InputType.dataModelFilterField, necessity = false)
    public List<QueryConditionDto> body;

    @ParameterValue(info = "分组字段", type = InputType.selected, necessity = false)
    public String groupBy;

    @ParameterValue(info = "聚合类型", necessity = false, type = InputType.selected, cls = AggregateTypeSelected.class)
    public AggregateEnumType type;

    @NotNull(message = "聚合字段不能为空")
    @ParameterValue(info = "聚合字段", type = InputType.selected)
    public String fields;


}
