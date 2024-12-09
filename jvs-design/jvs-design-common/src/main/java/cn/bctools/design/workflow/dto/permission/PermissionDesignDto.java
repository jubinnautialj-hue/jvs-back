package cn.bctools.design.workflow.dto.permission;

import cn.bctools.design.workflow.entity.FlowDesign;
import cn.bctools.design.workflow.model.Node;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang
 * 有权限启动的工作流列表响应-工作流信息
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel("有权限启动的工作流列表响应-工作流信息")
public class PermissionDesignDto extends FlowDesign {

    @ApiModelProperty("人工节点集合")
    private List<Node> manualNodes;

    @ApiModelProperty("true-可以增加节点，false-不能增加节点")
    private Boolean canDynamicAddNode;
}
