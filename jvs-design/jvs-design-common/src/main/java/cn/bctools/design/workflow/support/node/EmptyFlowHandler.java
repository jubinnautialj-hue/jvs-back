package cn.bctools.design.workflow.support.node;

import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.support.impl.AbstractFlowHandler;
import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import org.springframework.stereotype.Component;

/**
 * @author zhuxiaokang
 * 空节点处理
 */
@Component
public class EmptyFlowHandler extends AbstractFlowHandler {

    @Override
    public NodeTypeEnum getType() {
        return NodeTypeEnum.EMPTY;
    }

    @Override
    protected void handle(Node node, RuntimeData runtimeData) {
        FlowContextUtil.refreshContext(new FlowResult().setNextNode(node));
    }
}
