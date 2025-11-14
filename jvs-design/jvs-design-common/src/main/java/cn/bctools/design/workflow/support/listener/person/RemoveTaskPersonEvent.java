package cn.bctools.design.workflow.support.listener.person;

import cn.bctools.design.workflow.entity.FlowTask;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * @author zhuxiaokang
 * 移除审批人
 */
@Getter
@Setter
public class RemoveTaskPersonEvent extends ApplicationEvent {
    private FlowTask flowTask;
    private List<String> taskPersonIds;

    public RemoveTaskPersonEvent(Object source,FlowTask flowTask, List<String> taskPersonIds) {
        super(source);
        this.taskPersonIds = taskPersonIds;
        this.flowTask = flowTask;
    }

}
