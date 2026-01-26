package cn.bctools.design.workflow.support.listener.notify;

import cn.bctools.design.notice.handler.enums.TriggerTypeEnum;
import cn.bctools.design.project.dto.SwitchModeDto;
import cn.bctools.design.workflow.entity.FlowTask;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * @author zhuxiaokang
 * 事件——流程相关通知
 */
@Getter
@Setter
public class FlowNotifyEvent extends ApplicationEvent {
    private FlowTask flowTask;
    private TriggerTypeEnum triggerType;
    private List<String> nodeIds;
    private String tenantId;
    private SwitchModeDto mode;

    public FlowNotifyEvent(Object source, FlowTask flowTask, TriggerTypeEnum triggerType, List<String> nodeIds, String tenantId, SwitchModeDto mode) {
        super(source);
        this.flowTask = flowTask;
        this.nodeIds = nodeIds;
        this.triggerType = triggerType;
        this.tenantId = tenantId;
        this.mode = mode;
    }
}
