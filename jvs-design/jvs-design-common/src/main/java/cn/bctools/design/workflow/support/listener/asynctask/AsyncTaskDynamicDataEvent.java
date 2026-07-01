package cn.bctools.design.workflow.support.listener.asynctask;

import cn.bctools.design.workflow.entity.FlowTask;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * @author zhuxiaokang
 * 事件——同步流程数据到业务
 */
@Getter
@Setter
public class AsyncTaskDynamicDataEvent extends ApplicationEvent {
    private FlowTask flowTask;

    public AsyncTaskDynamicDataEvent(Object source, FlowTask flowTask) {
        super(source);
        this.flowTask = flowTask;
    }
}
