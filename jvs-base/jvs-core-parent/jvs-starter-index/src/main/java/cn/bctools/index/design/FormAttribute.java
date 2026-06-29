package cn.bctools.index.design;

import cn.bctools.index.design.enums.FormAttributeTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author jvs 每个组件需要显示的表单属性
 */
@Data
@Accessors(chain = true)
public class FormAttribute {

    /**
     * 属性显示的名称
     */
    private String label;

    /**
     * 属性 类型
     */
    private FormAttributeTypeEnum type;

    /**
     * 属性key
     */
    private String prop;
    /**
     * 描述信息
     */
    private String desc;

    /**
     * 限制值
     */
    private List<SelectedAttribute> dicData;
    /**
     * 关联数据选择的值
     */
    private List<String> linkProp;

    /**
     * 校验设置
     */
    private List<ValidatorAttribute> validator;
}
