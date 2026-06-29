package cn.bctools.design.data.source.impl.sql.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author: ZhuXiaoKang
 * @Description: 表字段缓存
 */
@Data
@Accessors(chain = true)
public class TableColumnCacheDto {

    @ApiModelProperty(value = "列名")
    private String columnName;

    @ApiModelProperty(value = "字段类型")
    private String jdbcType;


}
