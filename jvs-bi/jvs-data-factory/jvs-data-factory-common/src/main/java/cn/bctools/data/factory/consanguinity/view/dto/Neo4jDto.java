package cn.bctools.data.factory.consanguinity.view.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author xiaohui
 */
@Data
@ApiModel("前端需要的数据结构")
@Accessors(chain = true)
public class Neo4jDto {
    @ApiModelProperty("节点数据")
    List<Neo4jNodeDto> node;
    @ApiModelProperty("线条数据")
    List<Neo4jEdgesDto> edges;
}
