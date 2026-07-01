package cn.bctools.design.rule.impl.datamodel.update;

import cn.bctools.design.data.fields.dto.QueryConditionDto;
import cn.bctools.design.rule.impl.datamodel.DataModelLinkSelected;
import cn.bctools.design.rule.impl.datamodel.DataModelSelected;
import cn.bctools.rule.annotations.Inspect;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author guojing
 * 应用获取的数据，通过参数可以指定，不由参数去定制
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@Inspect
@NoArgsConstructor
public class DataModelUpdateDto {

    @NotNull(message = "数据模型不能为空")
    @ParameterValue(info = "数据模型", explain = "选择当前应用下的模型", type = InputType.selected, cls = DataModelSelected.class, linkFields = {"body", "query"}, linkCls = DataModelLinkSelected.class)
    public String dataModelId;

    @ParameterValue(info = "字段设置", type = InputType.dataModelField)
    @NotNull(message = "数据不能为空")
    public Map<String, Object> body;

    @ParameterValue(info = "查询条件", explain = "建议先判断条件值是否存在,值为空可能会导致错误", type = InputType.dataModelFilterField, necessity = false)
    public List<QueryConditionDto> query;

}
