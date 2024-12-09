package cn.bctools.design.workflow.entity.dto;

import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 * 工作流任务可执行路径节点
 */
@Data
@Accessors(chain = true)
@ApiModel("工作流任务可执行路径节点")
public class FlowPathNodeDto {

    @ApiModelProperty(value = "节点id")
    private String id;

    @ApiModelProperty(value = "节点名称")
    private String name;

    @ApiModelProperty(value = "节点类型")
    private NodeTypeEnum type;

}
