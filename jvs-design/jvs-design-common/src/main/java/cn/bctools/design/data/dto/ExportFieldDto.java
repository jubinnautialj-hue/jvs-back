package cn.bctools.design.data.dto;

import cn.bctools.design.data.fields.enums.DataFieldType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ExportFieldDto {
    @ApiModelProperty("字段名")
    String field;
    @ApiModelProperty("组件类型")
    DataFieldType type;
    @ApiModelProperty("具体的值")
    Object object;
}
