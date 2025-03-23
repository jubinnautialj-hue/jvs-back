package cn.bctools.design.rule.impl.form;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class FormStructureDto {

    @ParameterValue(info = "表单", explain = "选择表单", type = InputType.selected, cls = DesignNameSelected.class, linkFields = {"body"}, linkCls = FormStructureLinkSelected.class)
    public String designId;

    @ParameterValue(info = "字段设置", type = InputType.dataModelField)
    @NotNull(message = "数据不能为空")
    public Map<String, Object> body;

}
