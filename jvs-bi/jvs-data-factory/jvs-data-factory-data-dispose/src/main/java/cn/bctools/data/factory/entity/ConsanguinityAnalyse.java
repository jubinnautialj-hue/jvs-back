package cn.bctools.data.factory.entity;

import cn.bctools.data.factory.enums.ConsanguinityViewTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author : admin
 */
@Data
@ApiModel("血缘关系")
@Accessors(chain = true)
public class ConsanguinityAnalyse {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("数据集id")
    private String dataFactoryId;

    @ApiModelProperty("用户设计的名称")
    private String designName;
    @ApiModelProperty("用户设计的id")
    private String designId;

    @ApiModelProperty("单个图表的名称")
    private String designDetailName;

    @ApiModelProperty("租户id")
    private String tenantId;
    @ApiModelProperty("请求方类型")
    private ConsanguinityViewTypeEnum viewType;

    @ApiModelProperty("类型-来源1本身2去向3")
    private Integer type;
    @ApiModelProperty("资源id")
    private String designDetailId;


}


