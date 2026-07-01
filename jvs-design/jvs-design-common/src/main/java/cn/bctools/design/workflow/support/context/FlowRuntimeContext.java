package cn.bctools.design.workflow.support.context;

import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.process.ProcessResult;
import org.springframework.stereotype.Component;

/**
 * @author zhuxiaokang
 * 工作流运行时上下文
 */
@Component
public class FlowRuntimeContext extends AbstractFlowContext {

    @Override
    public void startFlow() {
        FlowContext flowContext = getContext();
        flowContext.setStart(Boolean.TRUE);
        flowContext.setApprovalTask(Boolean.FALSE);
        refresh(flowContext);
    }

    @Override
    public void refresh(FlowContext flowContext) {
        setContext(flowContext);
    }

    @Override
    public RuntimeData getRuntimeData() {
        return getContext().getRuntimeData();
    }

    @Override
    public FlowResult getFlowResult() {
        return getContext().getFlowResult();
    }

    @Override
    public ProcessResult getProcessResult() {
        return getContext().getProcessResult();
    }
}
