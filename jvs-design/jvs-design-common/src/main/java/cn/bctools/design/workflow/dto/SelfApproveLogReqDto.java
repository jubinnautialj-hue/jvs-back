package cn.bctools.design.workflow.dto;

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

}
