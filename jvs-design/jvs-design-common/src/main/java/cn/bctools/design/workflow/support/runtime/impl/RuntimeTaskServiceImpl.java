package cn.bctools.design.workflow.support.runtime.impl;

import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.enums.FlowTaskStatusEnum;
import cn.bctools.design.workflow.service.FlowTaskService;
import cn.bctools.design.workflow.service.TaskStopService;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.context.FlowContext;
import cn.bctools.design.workflow.support.listener.asynctask.AsyncTaskDynamicDataEvent;
import cn.bctools.design.workflow.support.listener.taskend.TaskEndEvent;
import cn.bctools.design.workflow.support.runtime.RuntimeTaskPathService;
import cn.bctools.design.workflow.support.runtime.RuntimeTaskService;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @author zhuxiaokang
 */
@Component
@AllArgsConstructor
public class RuntimeTaskServiceImpl implements RuntimeTaskService {


    private final TaskStopService taskStopService;
    private final FlowTaskService flowTaskService;
    private final RuntimeTaskPathService taskPathRuntimeService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void refreshDesign(FlowTask flowTask) {
        flowTaskService.updateById(flowTask);
        taskPathRuntimeService.refreshTaskPath(flowTask);
    }

    @Override
    public void endTask(FlowTask task, FlowTaskStatusEnum status, String reason) {
        FlowContext flowContext = FlowContextUtil.context().getContext();
        RuntimeData runtimeData = flowContext.getRuntimeData();
        // 启动流程
        if (flowContext.getStart()) {
            task.setTaskStatus(status);
            task.setStopReason(reason);
            runtimeData.setFlowTask(task);
            FlowContextUtil.refreshContext(runtimeData);
        } else {
            taskStopService.endTask(task, status, reason);
        }
        // 发布事件：任务结束
        applicationEventPublisher.publishEvent(new TaskEndEvent(this, task, runtimeData.getFlowExtend()));
        // 发布事件：同步流程信息到业务数据
        applicationEventPublisher.publishEvent(new AsyncTaskDynamicDataEvent(this, task));
    }
}
