package cn.bctools.design.rule.impl.datamodel.convert;


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
import java.util.Map;

/**
 * @author gx
 * 将模型数据转换为显示数据
 */
@Data
@Inspect
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class DataModelConvertShowDto {

    @NotNull(message = "数据模型不能为空")
    @ParameterValue(info = "数据模型", explain = "选择当前应用下的模型", type = InputType.selected, cls = DataModelSelected.class)
    public String dataModelId;

    @NotNull(message = "数据不能为空")
    @ParameterValue(info = "数据", type = InputType.listMap)
    public List<Map<String, Object>> body;

    @ParameterValue(info = "是否替换原始值", explain = "数据用于表单回显时，不替换原始值。数据用于生成模板文件时需要替换原始值。", type = InputType.selected, cls = BooleanSelected.class)
    public Boolean replace = true;
}
