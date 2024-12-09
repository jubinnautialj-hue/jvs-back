package cn.bctools.design.data.fields.dto.form;

import cn.bctools.design.data.fields.impl.ISelectorDataHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 键值对
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "键值对")
public class FormValueHtml {

    @ApiModelProperty("数据值")
    private String value;

    @ApiModelProperty("选项卡的数据值")
    private String name;

    @ApiModelProperty("显示数据")
    private String label;

    @ApiModelProperty("树形显示父级字段key")
    private String secTitle;

    /**
     * 由于字段关联模型保存的数据id，导致关联的模型字段关联了其它模型时，该字段的回显只能是数据id。
     * 如：
     * A模型有字段a
     * B模型有下拉框字段b，关联模型A的字段a
     * C模型有下拉框字段c，关联模型B的字段b。  此时C模型的字段c回显是数据id，但需求返回B模型的字段b的回显字段（A模型的字段a）
     * <p>
     * 此字段用以处理上述问题
     */
    @ApiModelProperty(value = "显示来源字段id")
    private String sourceFieldId;
    @ApiModelProperty(value = "开启了脱离数据后的属性值名")
    private String prop;
    @ApiModelProperty(value = "下级属性")
    List<FormValueHtml> children;

}
