package cn.bctools.design.workflow.dto;

import cn.bctools.design.workflow.entity.dto.TransferDto;
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
@ApiModel("工作流任务管理-转交入参")
public class FlowTaskManageTransferReqDto {
    @ApiModelProperty(value = "节点id", required = true)
    @NotBlank(message = "未选择审批节点")
    private String nodeId;

    @ApiModelProperty(value = "转交", required = true)
    @NotNull(message = "未设置转交人")
    private List<TransferDto> transfers;
}
