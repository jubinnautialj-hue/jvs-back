package cn.bctools.data.factory.consanguinity.view.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author xiaohui
 */
@Data
@ApiModel("前端需要的数据结构-线条数据")
@Accessors(chain = true)
public class Neo4jEdgesDto {
    @ApiModelProperty("来源节点")
    private String source;
    @ApiModelProperty("目标节点")
    private String target;
}
