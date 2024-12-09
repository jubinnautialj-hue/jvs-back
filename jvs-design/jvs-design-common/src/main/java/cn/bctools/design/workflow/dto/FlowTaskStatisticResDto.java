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
@ApiModel("工作流任务统计响应")
public class FlowTaskStatisticResDto {

    @ApiModelProperty(value = "我的待办任务数量")
    private Integer pendingCount;

    @ApiModelProperty(value = "我创建的任务数量")
    private Integer selfCreateCount;

    @ApiModelProperty(value = "抄送给我的任务数量")
    private Integer carbonCopyCount;

    @ApiModelProperty(value = "我参与审批的任务数量")
    private Integer selfApproveCount;


}
