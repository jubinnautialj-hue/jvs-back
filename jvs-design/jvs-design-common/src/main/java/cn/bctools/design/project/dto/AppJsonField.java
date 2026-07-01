package cn.bctools.design.project.dto;

import cn.bctools.design.data.fields.enums.DataFieldType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class AppJsonField {
    @ApiModelProperty("字段名")
    String field;
    @ApiModelProperty("中文名")
    String name;
    @ApiModelProperty("字段类型")
    String type;
    @ApiModelProperty("字段类型")
    DataFieldType fieldType;
}
