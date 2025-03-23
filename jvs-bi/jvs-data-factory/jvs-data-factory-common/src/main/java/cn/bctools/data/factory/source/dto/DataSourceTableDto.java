package cn.bctools.data.factory.source.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 数据源结构
 *
 * @Author: admin
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "数据源结构")
public class DataSourceTableDto {
    @ApiModelProperty(value = "表名称")
    private String name;
    @ApiModelProperty(value = "数据字段")
    private List<DataSourceTableStructureDto> structureList;

}
