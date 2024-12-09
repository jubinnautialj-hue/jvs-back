package cn.bctools.design.workflow.support.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.context.FlowContext;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import cn.bctools.design.workflow.utils.FlowUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author zhuxiaokang
 * 工作流流转处理
 */
@Slf4j
public abstract class AbstractCompositeFlowHandler {

    /**
     * 流程流转
     *
     * @param runtimeData 运行时参数
     */
    protected void execute(RuntimeData runtimeData) {
        try {
            // 工作流设计json解析
            FlowUtil.parseNodeJsonAndCache(runtimeData.getFlowTask().getDesignBody());
            // 找到当前执行的节点
            Node currentNode = FlowUtil.findNode(runtimeData.getNodeId(), StringUtils.isBlank(runtimeData.getNodeId()));
            // 任务流转
            execute(currentNode, runtimeData);
            // 后置处理
            after();
        } catch (Exception e) {
            log.error("Flow Exception：", e);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * 流程流转
     *
     * @param currentNode 当前节点基本数据
     * @param runtimeData 运行时参数
     */
    protected void execute(Node currentNode, RuntimeData runtimeData) {
        // 重置上下文
        FlowContextUtil.reset();
        // 当前节点不是开始节点
        if (Boolean.FALSE.equals(NodeTypeEnum.ROOT.equals(currentNode.getType()))) {
            // 则设置正在处理的节点id
            runtimeData.setNodeId(currentNode.getId());
            // 若当前节点的下级节点为空，则重新获取当前节点
            if (ObjectUtil.isNull(currentNode.getNode())) {
                currentNode = FlowUtil.findNode(runtimeData.getNodeId());
            }
        }
        runtimeData.setCurrentNode(currentNode);

        // 更新上下文
        FlowContextUtil.refreshContext(runtimeData);
        // 执行节点逻辑
        processNode(currentNode, runtimeData);
        FlowContext flowContext = FlowContextUtil.context().getContext();
        // 串行/并行
        if (ObjectNull.isNotNull(flowContext.getParallelResult())) {
            List<FlowResult> parallelList = BeanCopyUtil.copys(flowContext.getParallelResult(), FlowResult.class);
            // 重置上下文（否则并行分支执行时数据会冲突）
            FlowContextUtil.reset();
            Node parallelNode = BeanCopyUtil.copy(currentNode, Node.class);
            parallelList.forEach(flowResult -> {
                // 单个并行子分支执行时，使用对应的数据
                FlowContextUtil.refreshContext(flowResult);
                // 设置当前并行分支作为后续子分支首次执行的上一个节点（否则不能正常流转）
                runtimeData.setCurrentNode(parallelNode);
                FlowContextUtil.refreshContext(runtimeData);
                processFlow(flowResult);
            });
        } else {
            // 流转处理
            processFlow(flowContext.getFlowResult());
        }
    }

    /**
     * 任务流转
     *
     * @param flowResult 节点处理结果
     */
    private void processFlow(FlowResult flowResult) {
        // 流转。根据节点处理结果流转任务
        flow(flowResult);
        // 自动审批 (若后续节点满足自动审批条件，则执行自动审批)
        autoApproval();
        // 根据流转处理状态继续处理
        processFlowState();
    }


    /**
     * 执行节点逻辑
     *
     * @param currentNode 当前执行的节点
     * @param runtimeData 运行时数据
     */
    protected abstract void processNode(Node currentNode, RuntimeData runtimeData);

    /**
     * 流转。根据节点处理结果流转任务
     *
     * @param flowResult 节点处理结果
     */
    protected abstract void flow(FlowResult flowResult);

    /**
     * 自动审批 (若后续节点满足自动审批条件，则执行自动审批)
     *
     */
    protected abstract void autoApproval();

    /**
     * 根据流转处理状态继续处理
     */
    protected abstract void processFlowState();

    /**
     * 审批后置处理
     */
    protected abstract void after();
}
