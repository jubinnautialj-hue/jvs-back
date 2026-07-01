package cn.bctools.design.workflow.support.listener.taskend;

import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.dto.FlowExtendDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * @author zhuxiaokang
 */
@Getter
@Setter
public class TaskEndEvent extends ApplicationEvent {
    private FlowTask flowTask;
    private FlowExtendDto extend;

    public TaskEndEvent(Object source, FlowTask flowTask, FlowExtendDto extend) {
        super(source);
        this.flowTask = flowTask;
        this.extend = extend;
    }
}
