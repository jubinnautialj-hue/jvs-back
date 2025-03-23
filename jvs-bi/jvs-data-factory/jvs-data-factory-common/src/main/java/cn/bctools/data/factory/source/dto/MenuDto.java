package cn.bctools.data.factory.source.dto;


import cn.bctools.data.factory.source.entity.DataSource;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author admin
 */
@Data
@Accessors(chain = true)
@ApiModel("数据源菜单结构")
public class MenuDto {
    @ApiModelProperty("id")
    private String id;
    @ApiModelProperty("显示的名称")
    private String label;
    @ApiModelProperty("类型名称")
    private String sourceType;
    @ApiModelProperty("类型名称")
    private String sourceName;
    @ApiModelProperty("是否为根节点")
    private String type = "sourceRoot";
    @ApiModelProperty("id")
    private List<DataSource> children;
}
