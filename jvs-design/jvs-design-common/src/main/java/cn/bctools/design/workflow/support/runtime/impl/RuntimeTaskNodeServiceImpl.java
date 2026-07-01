package cn.bctools.design.workflow.support.runtime.impl;

import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.entity.enums.ProcessStatusEnum;
import cn.bctools.design.workflow.service.FlowTaskNodeService;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.context.FlowContext;
import cn.bctools.design.workflow.support.runtime.RuntimeTaskNodeService;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Optional;

/**
 * @author zhuxiaokang
 */
@Component
@AllArgsConstructor
public class RuntimeTaskNodeServiceImpl implements RuntimeTaskNodeService {


    private final FlowTaskNodeService flowTaskNodeService;

    @Override
    public FlowTaskNode saveResult(ProcessStatusEnum processStatusEnum) {
        FlowContext flowContext = FlowContextUtil.context().getContext();
        RuntimeData runtimeData = flowContext.getRuntimeData();
        // 节点id为空，开始节点，不保存数据，否则保存当前节点的处理结果
        if (runtimeData.getNodeId() == null) {
            return null;
        }
        FlowTaskNode flowTaskNode = null;
        // 启动流程
        if (flowContext.getStart()) {
            // 保存当前节点处理结果
            Optional<FlowTaskNode> flowTaskNodeOptional = Optional.ofNullable(runtimeData.getFlowTaskNodes()).orElseGet(ArrayList::new).stream().filter(node -> runtimeData.getNodeId().equals(node.getNodeId())).findFirst();
            if (Boolean.FALSE.equals(flowTaskNodeOptional.isPresent())) {
                return null;
            }
            flowTaskNode = flowTaskNodeOptional.get();
            flowTaskNode.setProcessStatus(processStatusEnum);
            flowTaskNodeService.assignCourse(flowTaskNode);
            return flowTaskNode;
        } else {
            return flowTaskNodeService.saveResult(processStatusEnum);
        }

    }
}
