package cn.bctools.design.workflow.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
@ApiModel("工作流任务管理-减员审批入参")
public class FlowTaskManageSignerRemoveReqDto {
    @ApiModelProperty(value = "节点id", required = true)
    @NotBlank(message = "未选择审批节点")
    private String nodeId;

    @ApiModelProperty(value = "减员id集合", required = true)
    @NotNull(message = "未减员")
    private List<String> userIds;
}
