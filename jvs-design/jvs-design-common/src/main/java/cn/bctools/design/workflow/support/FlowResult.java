package cn.bctools.design.workflow.support;

import cn.bctools.design.workflow.entity.enums.FlowTaskStatusEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.support.enums.FlowNextTypeEnum;
import cn.bctools.design.workflow.utils.FlowUtil;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 * 流转结果
 */

@Data
@Accessors(chain = true)
public class FlowResult {

    /**
     * 节点流转类型
     */
    private FlowNextTypeEnum flowNextTypeEnum;

    /**
     * 下一步流转节点
     */
    private Node node;

    /**
     * 工作流任务状态（结束工作流时，需指定工作流状态，若未指定，则默认为通过）
     */
    private FlowTaskStatusEnum flowTaskStatus;

    /**
     * TRUE-处理结束，FALSE-处理中
     * 由于增加了“加签”功能，导致一个节点可能有多批（一个加签一批，一个审核节点一批）审批。 此属性用于标识是否结束一批审批，若已结束，则修改该节点审批结果为已结束，避免影响ModeFunction审批逻辑
     */
    private boolean over = Boolean.FALSE;

    /**
     * 审批节点通过率
     */
    private Long passRate;

    /**
     * 设置下级节点和流转类型
     *
     * @param node 当前节点
     */
    public FlowResult setNextNode(Node node) {
        // 若有下级node，则流转到下级节点；
        // 若无下级node，判断是否有分支外层nodeId，若有则根据nodeId获取节点，否则视为结束节点
        this.node = FlowUtil.getNextNode(node);
        this.flowNextTypeEnum = this.node != null ? FlowNextTypeEnum.NEXT : FlowNextTypeEnum.END;
        return this;
    }
}
