package cn.bctools.design.rule.impl.form;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;


/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class FormFieldDynamicDto {

    @ParameterValue(info = "表单或列表页",explain = "可支持选择多个不同的表单或列表页对其添加对应的组件信息。",type = InputType.multipleSelected, cls = DesignNameSelected.class)
    public List<String> designId;
    @ParameterValue(info = "字段名组信息",explain = "动态添加的组件所有属性信息数据，将表单[动态表单]组件数据赋于此属性。",type = InputType.formula)
    public List<Map> list;

}
