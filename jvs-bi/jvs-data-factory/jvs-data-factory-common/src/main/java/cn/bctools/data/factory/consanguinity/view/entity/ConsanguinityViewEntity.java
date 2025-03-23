package cn.bctools.data.factory.consanguinity.view.entity;

import cn.bctools.data.factory.enums.ConsanguinityViewTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel("血缘视图实体类")
public class ConsanguinityViewEntity {

    @ApiModelProperty("唯一id组id+设计id+二级id的md5值")
    private String id;
    @ApiModelProperty("名称 设计名称")
    private String title;

    @ApiModelProperty("设计id")
    private String designId;

    @ApiModelProperty("下级名称 图表设计的单个图表名称")
    private String subordinateTile;

    @ApiModelProperty("下级id 图表设计的单个图表id")
    private String subordinateId;

    @ApiModelProperty("租户id")
    private String tenantId;
    @ApiModelProperty("组id-用于确认是否为同一个视图(其实就是本身id)")
    private String groupId;


    @ApiModelProperty("类型-大屏报表等等")
    private ConsanguinityViewTypeEnum viewType;
    @ApiModelProperty("类型-来源1本身2去向3")
    private Integer type;
}
