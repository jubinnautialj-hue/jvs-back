package cn.bctools.data.factory.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel("复制数据集入参")
public class CopyDto {
    @ApiModelProperty("菜单id")
    private String menuId;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("数据集id")
    private String id;
}
