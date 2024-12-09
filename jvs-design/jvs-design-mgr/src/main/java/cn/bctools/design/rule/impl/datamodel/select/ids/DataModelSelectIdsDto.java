package cn.bctools.design.rule.impl.datamodel.select.ids;

import cn.bctools.design.rule.impl.datamodel.DataModelLinkSelected;
import cn.bctools.design.rule.impl.datamodel.DataModelSelected;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class DataModelSelectIdsDto {

    @NotNull(message = "数据模型不能为空")
    @ParameterValue(info = "数据模型", explain = "选择当前应用下的模型", type = InputType.selected, cls = DataModelSelected.class, linkFields = {"fields", "body"}, linkCls = DataModelLinkSelected.class)
    public String dataModelId;

    @NotNull(message = "查询字段不能为空")
    @ParameterValue(info = "查询字段", necessity = false, type = InputType.multipleSelected)
    public Set<String> fields;

    @NotNull(message = "查询条件不能为空")
    @ParameterValue(info = "查询条件", type = InputType.list, necessity = false)
    public List ids;

}
