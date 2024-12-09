package cn.bctools.design.rule.impl.datamodel.save;

import cn.bctools.design.rule.impl.datamodel.DataModelLinkSelected;
import cn.bctools.design.rule.impl.datamodel.DataModelSelected;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author guojing
 * 应用获取的数据，通过参数可以指定，不由参数去定制
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class DataModelSaveDto {

    @NotNull(message = "数据模型不能为空")
    @ParameterValue(info = "数据模型", explain = "选择当前应用下的模型", type = InputType.selected, cls = DataModelSelected.class, linkFields = {"body"}, linkCls = DataModelLinkSelected.class)
    public String dataModelId;

    @ParameterValue(info = "字段设置", type = InputType.dataModelField)
    @NotNull(message = "数据不能为空")
    public Map<String, Object> body;

//    @ParameterValue(info = "一致性", type = InputType.multipleSelected, extension = {"add", "query"}, necessity = false, explain = "一致性操作, 如果存在多个数据操作的时候可以进行数据分组执行,同时失败和成功")
//    public List<String> transactional;

}
