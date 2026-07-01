package cn.bctools.design.workflow.support.process.impl;

import cn.bctools.design.workflow.entity.enums.FlowTaskStatusEnum;
import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.enums.FlowNextTypeEnum;
import cn.bctools.design.workflow.support.function.dto.ParallelDto;
import cn.bctools.design.workflow.support.function.impl.ParallelFunction;
import cn.bctools.design.workflow.support.process.ProcessInterface;
import cn.bctools.design.workflow.support.process.ProcessResult;
import cn.bctools.design.workflow.support.runtime.RuntimeTaskService;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author zhuxiaokang
 * 流程结束结果处理
 */
@Slf4j
@Component
@AllArgsConstructor
public class EndProcess implements ProcessInterface {

    private final RuntimeTaskService runtimeTaskService;
    private final ParallelFunction parallelFunction;


    @Override
    public FlowNextTypeEnum getType() {
        return FlowNextTypeEnum.END;
    }

    @Override
    public void execute(FlowResult flowResult) {
        // 若当前节点是并行节点，且并行任务未结束，则不进行后续处理
        if (Boolean.FALSE.equals(parallelFunction.invoke(flowResult.getNode(), new ParallelDto().setFlowResult(flowResult)))) {
            FlowContextUtil.refreshContext(ProcessResult.buildEndProcess());
            return;
        }
        RuntimeData runtimeData = FlowContextUtil.context().getRuntimeData();
        log.info("【结束】修改任务{}状态", runtimeData.getFlowTask().getId());
        // 工作流结束时，若未指定状态，则默认为”passed“
        FlowTaskStatusEnum flowTaskStatus = Optional.ofNullable(flowResult.getFlowTaskStatus()).orElse(FlowTaskStatusEnum.PASSED);

        // 任务结束
        runtimeTaskService.endTask(runtimeData.getFlowTask(), flowTaskStatus, null);
        FlowContextUtil.refreshContext(ProcessResult.buildEndProcess());
    }
}