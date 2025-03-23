package cn.bctools.data.factory.source.dto;

import cn.bctools.data.factory.source.dto.enums.DataSourceStructureEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author admin
 */
@Data
@Accessors(chain = true)
@ApiModel("数据库表结构")
public class DataSourceTableStructureDto {
    @ApiModelProperty("字段名称 此名称时对应表结构名称的不可以更改")
    private String name;
    @ApiModelProperty("解释此字段用户可以自定义")
    private String explain;
    @ApiModelProperty("是否显示")
    private String isHide;
    @ApiModelProperty("字段类型")
    private DataSourceStructureEnum type;
    @ApiModelProperty("顺序")
    private Integer order;
}
