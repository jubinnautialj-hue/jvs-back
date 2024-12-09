package cn.bctools.design.workflow.support.context;

import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.process.ProcessResult;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
public class FlowContext {

    /**
     * true-启动工作流，false-非启动工作流
     */
    private Boolean start = Boolean.FALSE;

    /**
     * true-已分配审批任务， false-未分配审批任务
     */
    private Boolean approvalTask;

    /**
     * 运行时数据
     */
    private RuntimeData runtimeData;

    /**
     * true-存在并行任务，false-不存在并行任务
     */
    private Boolean existsParallel;

    /**
     * 串行节点处理结果
     */
    private FlowResult flowResult;

    /**
     * 并行节点处理结果
     */
    private List<FlowResult> parallelResult;

    /**
     * 流转结果
     */
    private ProcessResult processResult;

}
