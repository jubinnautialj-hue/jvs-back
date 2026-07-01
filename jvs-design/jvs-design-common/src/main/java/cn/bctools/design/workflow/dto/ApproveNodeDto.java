package cn.bctools.design.workflow.dto;

import cn.bctools.design.workflow.model.Node;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel("任务进度响应-工作流设计JSON(含处理过程)")
public class ApproveNodeDto extends Node {
    @ApiModelProperty(value = "是否允许动态选择审批人")
    private Boolean canDynamicApprover;

}
