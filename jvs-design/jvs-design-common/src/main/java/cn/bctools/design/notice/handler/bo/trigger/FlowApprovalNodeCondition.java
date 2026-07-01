package cn.bctools.design.notice.handler.bo.trigger;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang
 * 审批节点到达指定节点时触发通知条件
 */
@Data
@Accessors(chain = true)
@ApiModel("审批节点到达指定节点时触发通知条件")
public class FlowApprovalNodeCondition {

    @ApiModelProperty(value = "节点id集合")
    private List<String> nodeIds;
}
