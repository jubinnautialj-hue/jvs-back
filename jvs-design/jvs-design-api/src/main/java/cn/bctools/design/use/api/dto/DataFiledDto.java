package cn.bctools.design.use.api.dto;

import cn.bctools.design.use.api.enums.DataFieldTypeDataEnum;
import cn.bctools.design.use.api.enums.DataFieldTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class DataFiledDto {

    @ApiModelProperty("表的全局标识")
    private String tableCode;

    private String modelId;

    @ApiModelProperty("字段名称")
    private String columnName;

    @ApiModelProperty("对应java的类型字段类型")
    private Class<?> cls;
    @ApiModelProperty("对应java的类型字段类型")
    private DataFieldTypeEnum type;
    private DataFieldTypeDataEnum dataType;
    @ApiModelProperty("长度-例如 varchar  datetime DECIMAL")
    private Integer length;
    @ApiModelProperty("精度-DECIMAL 类型")
    private Integer precision;
    @ApiModelProperty("字段解释")
    private String columnCount;
    @ApiModelProperty(value = "日期格式", example = "如:yyyy-MM-dd,默认都是38,4")
    private String format;
}
