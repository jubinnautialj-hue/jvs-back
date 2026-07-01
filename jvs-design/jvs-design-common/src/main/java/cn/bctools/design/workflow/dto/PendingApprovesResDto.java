package cn.bctools.design.workflow.dto;

import cn.bctools.design.workflow.entity.FlowTask;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel("待我审批列表查询响应")
public class PendingApprovesResDto extends FlowTask {

    @ApiModelProperty("当前环节审批任务节点唯一id")
    private String taskNodeId;

    @ApiModelProperty(value = "当前环节节点id")
    private String nodeId;

    @ApiModelProperty(value = "当前环节节点名")
    private String nodeName;

    @ApiModelProperty(value = "发起人头像")
    private String headImg;
}
