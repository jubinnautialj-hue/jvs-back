package cn.bctools.design.data.source.impl.sql.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author: ZhuXiaoKang
 * @Description: 数据字段
 */
@Data
@Accessors(chain = true)
public class DataColumnDto {

    @ApiModelProperty(value = "列名")
    private String columnName;

    @ApiModelProperty(value = "java类型")
    private Class<?> javaType;

    @ApiModelProperty(value = "sql片段")
    private String sqlSegment;

}
