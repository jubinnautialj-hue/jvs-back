package cn.bctools.design.notice.handler.bo.trigger;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang
 * 流程结束时，判断审批结果触发通知条件
 */
@Data
@Accessors(chain = true)
@ApiModel("审批结果触发通知条件")
public class FlowApprovalResultsCondition {

    @ApiModelProperty(value = "审批结果")
    private List<Integer> flowTaskStatus;
}
