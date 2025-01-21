package cn.bctools.design.data.fields.dto.form.item;

import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.FieldPublicHtml;
import cn.bctools.design.data.fields.dto.form.FormValueHtml;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "选项卡")
@EqualsAndHashCode(callSuper = true)
public class TabItemHtml extends BaseItemHtml {

    @ApiModelProperty("字典项")
    private List<FormValueHtml> dicData;
    @ApiModelProperty("多个选项卡的具体值")
    Map<String, List<FieldBasicsHtml>> column;
    /**
     * 数据脱离如果打开，则数据结构会发生变化 ，所有使用到公式获取表单值的都会根据此属性做两套路径获取的代码
     * 此开关用于同一个模型的多个字段，展示在不同的 tab 项中。避免使用 tab项后无法展示一个模型的字段
     */
    @ApiModelProperty("是否开启脱离数据")
    Boolean detachData = false;
    @ApiModelProperty("true每次只能展开一个面板 false可以同时展示开多个")
    Boolean accordion;

}
