package cn.bctools.design.workflow.dto.startflow;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhuxiaokang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("启动工作流变量")
public class StartFlowVariables extends StartFlowReqDto {
    @ApiModelProperty(value = "数据id")
    private String dataId;
    @ApiModelProperty(value = "数据模型id")
    private String modelId;
    @ApiModelProperty(value = "数据版本")
    private String dataVersion;
    @ApiModelProperty(value = "指定发起节点表单id")
    private String sendFormId;
}
