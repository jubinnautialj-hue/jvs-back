package cn.bctools.report.model.univer.conf;

import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.report.model.univer.UCellType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel("单元格中选择的字段数据")
public class UField {

    @ApiModelProperty("类型")
    private DataFieldTypeClassifyEnum datFieldTypeClassify;

    @ApiModelProperty("数据集id")
    private String executeName;

    @ApiModelProperty("字段key")
    private String fieldKey;

    @ApiModelProperty(value = "是否为mock数据",notes = "默认为false")
    private Boolean isMock = Boolean.FALSE;

    @ApiModelProperty("字段名称")
    private String fieldName;

    @ApiModelProperty("字段类型")
    private DataFieldTypeEnum fieldType;

    @ApiModelProperty("格式化")
    private String format;

    @ApiModelProperty("格式化 默认值")
    private String formatDefault;

    @ApiModelProperty("数据集名称")
    private String name;

    @ApiModelProperty("数据源类型")
    private String sourceType;

    public int getUCellT(){
        if(this.fieldType == null){
            return UCellType.FORCE;
        }
        DataFieldTypeClassifyEnum classifyEnum = this.fieldType.getClassifyEnum();
        switch (classifyEnum){
            case 字符串:
                return UCellType.STRING;
            case 时间:
            case 数字:
                return UCellType.NUMBER;
            case 布尔:
                return UCellType.BOOLEAN;
            default:
                return UCellType.FORCE;
        }
    }

}
