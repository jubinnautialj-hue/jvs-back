package cn.bctools.design.workflow.utils;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.context.FlowContext;
import cn.bctools.design.workflow.support.context.IFlowContext;
import cn.bctools.design.workflow.support.process.ProcessResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author zhuxiaokang
 * 工作流运行时上下文工具
 */
public class FlowContextUtil {

    private static IFlowContext flowContext = null;

    private FlowContextUtil () {

    }

    static {
        flowContext = SpringContextUtil.getBean(IFlowContext.class);
    }

    public static IFlowContext context() {
        return flowContext;
    }

    /**
     * 设置处理结果并刷新上下文
     *
     * @param flowResult 节点处理结果
     */
    public static void refreshContext(FlowResult flowResult) {
        FlowContext flowContext = context().getContext().setFlowResult(flowResult);
        context().refresh(flowContext);
    }

    /**
     * 设置并行节点处理结果并刷新上下文
     *
     * @param parallelResult 并行节点处理结果
     */
    public static void refreshContext(List<FlowResult> parallelResult) {
        FlowContext flowContext = context().getContext().setParallelResult(parallelResult);
        context().refresh(flowContext);
    }

    /**
     * 设置运行时数据并刷新上下文
     *
     * @param runtimeData 运行时数据
     */
    public static void refreshContext(RuntimeData runtimeData) {
        FlowContext flowContext = context().getContext().setRuntimeData(runtimeData);
        context().refresh(flowContext);
    }

    /**
     * 设置流转结果并刷新上下文
     * @param processResult 流转结果
     */
    public static void refreshContext(ProcessResult processResult) {
        FlowContext flowContext = context().getContext().setProcessResult(processResult);
        context().refresh(flowContext);
    }


    /**
     * 刷新流转过程中的任务节点
     *
     * @param flowTaskNode 任务节点
     */
    public static void refreshContext(FlowTaskNode flowTaskNode) {
        RuntimeData runtimeData = context().getContext().getRuntimeData();
        List<FlowTaskNode> flowTaskNodes = Optional.ofNullable(runtimeData.getFlowTaskNodes()).orElseGet(ArrayList::new);
        flowTaskNodes.removeIf(taskNode -> flowTaskNode.getId().equals(taskNode.getId()));
        flowTaskNodes.add(flowTaskNode);
        runtimeData.setFlowTaskNodes(flowTaskNodes);
        refreshContext(runtimeData);
    }

    /**
     * 刷新分配任务状态
     *
     * @param approvalTask  true-已分配审批任务， false-未分配审批任务
     */
    public static void refreshApprovalTask(Boolean approvalTask) {
        FlowContext flowContext = context().getContext().setApprovalTask(approvalTask);
        context().refresh(flowContext);
    }

    /**
     * 刷新是否存在并行任务
     *
     * @param existsParallel true-存在并行任务，false-不存在并行任务
     */
    public static void refreshExistsParallel(Boolean existsParallel) {
        FlowContext flowContext = context().getContext().setExistsParallel(existsParallel);
        context().refresh(flowContext);
    }


    /**
     * 重置上下文
     */
    public static void reset() {
        FlowContext flowContext = context().getContext()
                .setFlowResult(null)
                .setParallelResult(null)
                .setProcessResult(null);
        context().refresh(flowContext);
    }

}
