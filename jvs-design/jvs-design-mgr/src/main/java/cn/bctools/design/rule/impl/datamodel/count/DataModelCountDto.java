package cn.bctools.design.rule.impl.datamodel.count;

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

/**
 * @author guojing
 * 应用获取的数据，通过参数可以指定，不由参数去定制
 */
@Data
@Inspect
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class DataModelCountDto {

    @NotNull(message = "数据模型不能为空")
    @ParameterValue(info = "数据模型", explain = "选择当前应用下的模型", type = InputType.selected, cls = DataModelSelected.class, linkFields = {"fields", "body"}, linkCls = DataModelLinkSelected.class)
    public String dataModelId;

    @NotNull(message = "查询条件不能为空")
    @ParameterValue(info = "查询条件", type = InputType.dataModelFilterField, necessity = false)
    public List<QueryConditionDto> body;

}
