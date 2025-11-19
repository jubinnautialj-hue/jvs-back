package cn.bctools.design.workflow.dto;

import cn.bctools.design.workflow.entity.enums.FlowTaskStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("自己审批的任务记录")
public class SelfApproveLogReqDto {

    @ApiModelProperty(value = "流程名称")
    private String flowName;

    @ApiModelProperty(value = "发起人名称")
    private String sendUser;

    @ApiModelProperty(value = "流程编号")
    private String taskCode;
    @ApiModelProperty(value = "状态：1-待审批，2-已通过，3-已拒绝，4-已终止")
    private FlowTaskStatusEnum taskStatus;

    @ApiModelProperty(value = "流程ID")
    private String taskId;

}
