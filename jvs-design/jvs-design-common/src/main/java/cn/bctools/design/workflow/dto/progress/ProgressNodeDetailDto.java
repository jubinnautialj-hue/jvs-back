package cn.bctools.design.workflow.dto.progress;

import cn.bctools.design.workflow.entity.enums.FlowTaskNodeApprovalTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 * 进度节点信息
 */
@Data
@Accessors(chain = true)
@ApiModel("进度节点信息")
public class ProgressNodeDetailDto {
    @ApiModelProperty(value = "待办节点id")
    private String nodeId;

    @ApiModelProperty(value = "待办节点名")
    private String nodeName;

    @ApiModelProperty(value = "待办节点表单id")
    private String formId;

    @ApiModelProperty(value = "待办节点表单版本")
    private String formVersion;

    @ApiModelProperty(value = "待办节点审批类型：1-审批，2-加签审批")
    private FlowTaskNodeApprovalTypeEnum approvalType;
}
