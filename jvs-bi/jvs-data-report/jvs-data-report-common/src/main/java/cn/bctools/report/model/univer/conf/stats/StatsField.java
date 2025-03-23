package cn.bctools.report.model.univer.conf.stats;

import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class StatsField implements Serializable {

    private static final long serialVersionUID = 4715841024461591625L;

    @ApiModelProperty("统计字段key")
    private String statsFieldKey;

    @ApiModelProperty(value = "行号",notes = "统计字段单元格的行号")
    private Integer r;

    @ApiModelProperty(value = "列号",notes = "统计字段单元格的列号")
    private Integer c;

    @ApiModelProperty("统计字段名称")
    private String statsFieldName;

    @ApiModelProperty("字段类型分类")
    private DataFieldTypeClassifyEnum dataFieldTypeClassify;

    @ApiModelProperty("数据集id")
    private String executeName;
}
