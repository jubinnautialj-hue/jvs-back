package cn.bctools.design.workflow.support.node;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.design.workflow.model.Condition;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.enums.FlowNextTypeEnum;
import cn.bctools.design.workflow.support.function.impl.ConditionFunction;
import cn.bctools.design.workflow.support.impl.AbstractFlowHandler;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import cn.bctools.design.workflow.utils.FlowUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author zhuxiaokang
 * 条件节点处理
 */
@Component
@AllArgsConstructor
public class ConditionFlowHandler extends AbstractFlowHandler {

    private final ConditionFunction conditionFunction;

    @Override
    public NodeTypeEnum getType() {
        return NodeTypeEnum.CONDITION;
    }

    @Override
    protected void handle(Node node, RuntimeData runtimeData) {
        // 得到通过条件校验的条件分支节点
        Condition passCondition = Optional.ofNullable(conditionFunction.invoke(node, runtimeData))
                .orElseThrow(() -> new BusinessException("未找到满足条件的分支无法继续流转请检查工作流设计"));
        // 下一步节点
        Node next = passCondition.getNode();
        // 得到满足条件的分支
        if (FlowUtil.isNullNode(next)) {
            // 若已到分支底部，则最外层条件节点的下级节点为下一步应该流转的节点
            if (node.getNode() == null ) {
                next = FlowUtil.findNode(passCondition.getBranchNextNodeId());
            } else {
                // 判断下级节点是否有下级节点，若无，则最外层条件节点的下级节点为下一步应该流转的节点
                Node nextNode = FlowUtil.findNode(node.getNode().getId());
                next = FlowUtil.isNullNode(nextNode) ? FlowUtil.findNode(passCondition.getBranchNextNodeId()) : nextNode;
            }
        } else {
            // 得到下级节点，判断若是条件节点，则继续处理
            if (NodeTypeEnum.CONDITION.equals(next.getType())) {
                next = FlowUtil.findNode(next.getId());
                handle(next, runtimeData);
            }
        }
        FlowContextUtil.refreshContext(new FlowResult().setNode(next).setFlowNextTypeEnum(FlowNextTypeEnum.NEXT));
    }
}
