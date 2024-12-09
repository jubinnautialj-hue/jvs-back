package cn.bctools.design.workflow.support.listener.person;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.service.FlowTaskPersonService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * @author zhuxiaokang
 * 移除审批人监听
 */
@Slf4j
@Component
@AllArgsConstructor
public class RemoveTaskPersonListener {

    private final FlowTaskPersonService flowTaskPersonService;

    /**
     * 监听移除审批人事件
     *
     * @param event 事件对象
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskEndEvent(RemoveTaskPersonEvent event) {
        if (ObjectNull.isNull(event.getTaskPersonIds())) {
            return;
        }
        flowTaskPersonService.removeByIds(event.getTaskPersonIds());
    }

}
