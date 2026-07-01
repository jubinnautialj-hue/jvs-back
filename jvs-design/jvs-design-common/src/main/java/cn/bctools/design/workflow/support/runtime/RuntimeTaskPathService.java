package cn.bctools.design.workflow.support.runtime;

import cn.bctools.design.workflow.entity.FlowTask;

/**
 * @author zhuxiaokang
 * 工作流任务可执行路径运行时服务
 */

public interface RuntimeTaskPathService {

    /**
     * 刷新任务可执行路径
     *
     * @param flowTask 流程任务
     */
    void refreshTaskPath(FlowTask flowTask);

}
