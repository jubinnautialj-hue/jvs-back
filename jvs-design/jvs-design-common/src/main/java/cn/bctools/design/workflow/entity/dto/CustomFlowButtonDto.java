package cn.bctools.design.workflow.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author jvs
 */
@Data
@ApiModel("工作流设计扩展信息——全局审批按钮配置")
public class CustomFlowButtonDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "true-启用, false-禁用")
    private Boolean enable = Boolean.FALSE;

    @ApiModelProperty(value = "全局审批按钮配置")
    private List<FlowButtonDto> buttons;
}
