package cn.bctools.design.workflow.support.listener.person;

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

    private List<String> taskPersonIds;

    public RemoveTaskPersonEvent(Object source, List<String> taskPersonIds) {
        super(source);
        this.taskPersonIds = taskPersonIds;
    }

}
