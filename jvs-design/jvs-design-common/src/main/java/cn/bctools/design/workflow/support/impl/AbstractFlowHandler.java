package cn.bctools.design.workflow.support.impl;

import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.support.FlowInterface;
import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhuxiaokang
 * 工作流流转处理抽象类
 */
@Slf4j
public abstract class AbstractFlowHandler implements FlowInterface {

    @Override
    public void execute() {
        // 逻辑处理
        RuntimeData runtimeData = FlowContextUtil.context().getRuntimeData();
        Node node = runtimeData.getCurrentNode();
        handle(node, runtimeData);
    }

    /**
     * 不同类型的节点，自行实现处理逻辑
     * @param node 节点
     * @param runtimeData 运行时数据
     */
    protected void handle(Node node, RuntimeData runtimeData) {
        log.info("当前节点类型[{}]无具体实现", node.getType().getValue());
        FlowContextUtil.refreshContext(new FlowResult());
    }
}
