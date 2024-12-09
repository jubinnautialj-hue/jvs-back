package cn.bctools.design.workflow.support.runtime;

import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.enums.FlowTaskStatusEnum;

/**
 * @author zhuxiaokang
 */
public interface RuntimeTaskService {

    /**
     * 修改流程设计
     *
     * @param flowTask 任务对象
     */
    void refreshDesign(FlowTask flowTask);

    /**
     * 任务结束
     *
     * @param task   工作流任务
     * @param status 工作流任务状态
     * @param reason 终止任务原因
     */
    void endTask(FlowTask task, FlowTaskStatusEnum status, String reason);

}
