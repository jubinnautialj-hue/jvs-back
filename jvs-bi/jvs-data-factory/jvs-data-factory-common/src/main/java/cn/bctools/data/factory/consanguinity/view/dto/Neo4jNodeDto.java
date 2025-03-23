package cn.bctools.data.factory.consanguinity.view.dto;


import cn.bctools.data.factory.enums.ConsanguinityViewTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author xiaohui
 */
@Data
@ApiModel("前端需要的数据结构-节点数据")
@Accessors(chain = true)
public class Neo4jNodeDto {
    @ApiModelProperty("设计id")
    private String id;

    @ApiModelProperty("设计id")
    private String designId;

    @ApiModelProperty("名称 设计名称")
    private String title;

    @ApiModelProperty("是否被删除")
    private Boolean deleteIs;

    @ApiModelProperty("下级名称 图表设计的单个图表名称")
    private String subordinateTile;

    @ApiModelProperty("下级名称 图表设计的单个图表名称")
    private String subordinateId;

    @ApiModelProperty("租户id")
    private String tenantId;

    @ApiModelProperty("等级")
    private Integer level;

    @ApiModelProperty("类型")
    private ConsanguinityViewTypeEnum type;


}
