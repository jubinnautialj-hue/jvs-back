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
@ApiModel("查询自己发起的任务列表入参")
public class SelfPageReqDto {

    @ApiModelProperty(value = "任务名称")
    private String name;

    @ApiModelProperty(value = "任务标题")
    private String title;

    @ApiModelProperty(value = "状态")
    private FlowTaskStatusEnum taskStatus;

    @ApiModelProperty(value = "流程编号")
    private String taskCode;
}
