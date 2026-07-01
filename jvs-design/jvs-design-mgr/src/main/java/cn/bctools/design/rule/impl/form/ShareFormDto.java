package cn.bctools.design.rule.impl.form;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ShareFormDto {

    @ParameterValue(info = "表单或列表页", explain = "选择分享表单或列表页。", type = InputType.selected, cls = DesignNameSelected.class)
    public String designId;
    @ParameterValue(info = "数据id", explain = "分享携带的数据 id", necessity = false)
    public String id;

}
