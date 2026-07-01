package cn.bctools.design.workflow.dto;

import cn.bctools.design.workflow.entity.FlowTask;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel("工作流任务管理-流程任务分页响应")
public class PageFlowTaskManageResDto extends FlowTask {

    @ApiModelProperty("当前环节节点名")
    private String currentNodeName;

    @ApiModelProperty("部门名称")
    private String createDeptName;

    @ApiModelProperty("true-是发起节点，false-不是发起节点")
    private Boolean rootNode;

    @ApiModelProperty("有审批人的节点集合")
    private List<FlowTaskManageNodeDto> nodes;
}
