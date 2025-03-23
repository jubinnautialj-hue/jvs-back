package cn.bctools.data.factory.source.dto;

import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 模型数据源入参
 *
 * @author admin
 */
@Data
@Accessors(chain = true)
@ApiModel("模型数据源入参")
public class DataModelDataSourceDto {
    @ApiModelProperty("应用id")
    private String sourceLibraryName;
    @ApiModelProperty("模型名称")
    private String name;
    @ApiModelProperty("模式")
    private String mode;
    @ApiModelProperty("连接信息")
    private String datasource;
    @ApiModelProperty("数据源类型")
    private DataSourceTypeEnum sourceType;


}
