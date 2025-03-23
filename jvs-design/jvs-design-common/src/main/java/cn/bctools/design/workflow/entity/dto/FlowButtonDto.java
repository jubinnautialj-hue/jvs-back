package cn.bctools.design.workflow.entity.dto;

import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author jvs
 */
@Data
@ApiModel("工作流设计扩展信息——按钮配置")
public class FlowButtonDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "按钮操作类型")
    private NodeOperationTypeEnum operation;

    @ApiModelProperty(value = "按钮显示名称")
    private String displayName;
}
