package cn.bctools.data.factory.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Administrator
 */
@ApiModel("通过数据集获取关联数据id")
@Data
@Accessors(chain = true)
public class DownGetDataFactoryDto {
    @ApiModelProperty("数据id")
    private String id;
    @ApiModelProperty("是否为数据源")
    private Boolean isDataSource;
}
