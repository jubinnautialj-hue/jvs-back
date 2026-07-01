package cn.bctools.design.workflow.support.context;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * @author zhuxiaokang
 * 工作流上下文
 */
public abstract class AbstractFlowContext implements IFlowContext {
    /**
     * 工作流节点缓存
     * Map<节点id, 节点>
     */
    private static ThreadLocal<FlowContext> flowRuntimeContext = new TransmittableThreadLocal<>();

    @Override
    public void init() {
        setContext(new FlowContext());
    }

    @Override
    public void destroy() {
        clear();
    }

    @Override
    public FlowContext getContext() {
        return flowRuntimeContext.get();
    }

    protected void setContext(FlowContext context) {
        flowRuntimeContext.set(context);
    }

    private void clear() {
        flowRuntimeContext.remove();
    }

}
