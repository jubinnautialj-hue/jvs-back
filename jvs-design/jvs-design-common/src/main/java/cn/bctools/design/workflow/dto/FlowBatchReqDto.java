package cn.bctools.design.workflow.dto;

import cn.bctools.design.workflow.entity.dto.ApproveOpinionDto;
import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author zhuxiaokang 
 */
@Data
@Accessors(chain = true)
@ApiModel("工作流批量处理操作信息入参")
public class FlowBatchReqDto {

    @ApiModelProperty(value = "批量处理工作流任务id", required = true)
    @NotEmpty(message = "工作流任务id不能为空")
    private List<String> ids;

    @ApiModelProperty(value = "流转节点操作类型", required = true)
    @NotNull(message = "未传递操作类型")
    private NodeOperationTypeEnum nodeOperationType;

    @ApiModelProperty(value = "审批意见")
    private ApproveOpinionDto opinion;
}
