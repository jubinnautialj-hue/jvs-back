package cn.bctools.design.workflow.support.timelimit;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.design.workflow.entity.FlowTaskPerson;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zhuxiaokang
 * 审批期限逾期处理策略
 */
@Component
@AllArgsConstructor
public class CompositeOverdueHandler {

    private final List<OverdueInterface> overdueInterfaceList;

    /**
     * 审批超时处理
     * @param msg 消息
     * @param flowTaskPerson 审批人信息
     */
    public void overdueProcess(TimeLimitMessageDto msg, FlowTaskPerson flowTaskPerson) {
        overdueInterfaceList.stream()
                .filter(overdue -> overdue.getType().equals(msg.getTimeLimit().getEvent().getType()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("审批超时处理方式暂未支持"))
                .process(msg, flowTaskPerson);
    }
}
