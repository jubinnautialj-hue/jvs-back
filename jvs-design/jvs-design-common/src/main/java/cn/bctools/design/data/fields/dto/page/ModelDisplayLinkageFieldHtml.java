package cn.bctools.design.data.fields.dto.page;

import cn.bctools.design.data.fields.enums.DataFieldType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author jvs
 * 显示方式-配置显示关联模型的数据——要显示的字段
 */
@Data
public class ModelDisplayLinkageFieldHtml {
    @ApiModelProperty(value = "字段Key", notes = "模型字段key")
    private String prop;

    @ApiModelProperty(value = "模型字段key别名", notes = "防止模型字段key与当前模型中的字段重名，导致无法获取数据")
    private String aliasProp;

    @ApiModelProperty("字段类型")
    private DataFieldType type;

    @ApiModelProperty("字段名")
    private String label;
    @ApiModelProperty("高级设置")
    private DataTableFieldAdvancedSettingsHtml advancedSettings;

}
