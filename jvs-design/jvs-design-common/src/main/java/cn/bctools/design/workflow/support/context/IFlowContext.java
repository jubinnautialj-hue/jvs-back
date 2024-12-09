package cn.bctools.design.workflow.support.context;

import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.process.ProcessResult;

/**
 * @author zhuxiaokang
 * 工作流上下文操作
 */
public interface IFlowContext {

    /**
     * 初始化工作流运行时上下文
     */
    void init();

    /**
     * 销毁工作流运行时上下文
     */
    void destroy();

    /**
     * 设置为发起工作流
     */
    void startFlow();

    /**
     * 获取工作流运行时上下文
     *
     * @return 工作流运行时上下文
     */
    FlowContext getContext();

    /**
     * 刷新工作流运行时上下文
     *
     * @param flowContext 工作流运行时上下文
     */
    void refresh(FlowContext flowContext);

    /**
     * 获取工作流上下文中的运行时数据
     *
     * @return 运行时数据
     */
    RuntimeData getRuntimeData();

    /**
     * 获取串行节点处理结果
     *
     * @return 串行节点处理结果
     */
    FlowResult getFlowResult();

    /**
     * 获取流转结果
     *
     * @return 流转结果
     */
    ProcessResult getProcessResult();


}
