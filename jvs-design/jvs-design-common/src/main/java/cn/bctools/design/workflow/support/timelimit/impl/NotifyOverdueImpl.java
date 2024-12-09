package cn.bctools.design.workflow.support.timelimit.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.notice.handler.send.DataNotifyDto;
import cn.bctools.design.notice.handler.send.impl.SystemSendNotifyImpl;
import cn.bctools.design.workflow.entity.FlowTaskPerson;
import cn.bctools.design.workflow.model.enums.TimeLimitEventTypeEnum;
import cn.bctools.design.workflow.model.enums.TimeLimitTypeEnum;
import cn.bctools.design.workflow.support.timelimit.OverdueInterface;
import cn.bctools.design.workflow.support.timelimit.TimeLimitMessageHandler;
import cn.bctools.design.workflow.support.timelimit.TimeLimitMessageDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhuxiaokang
 * 发送通知
 */
@Component
@AllArgsConstructor
public class NotifyOverdueImpl implements OverdueInterface {

    private final TimeLimitMessageHandler timeLimitMessageHandler;
    private final SystemSendNotifyImpl systemSendNotify;

    public static final String NOTIFY_TITLE = "工作流审批提醒";
    private static final String MESSAGE_TEMPLATE = "【%s】的工作流任务【%s】等待处理";

    @Override
    public TimeLimitEventTypeEnum getType() {
        return TimeLimitEventTypeEnum.NOTIFY;
    }

    @Override
    public void process(TimeLimitMessageDto msg, FlowTaskPerson flowTaskPerson) {
        List<UserDto> userDtos = new ArrayList<>();
        userDtos.add(new UserDto().setId(flowTaskPerson.getUserId()).setRealName(flowTaskPerson.getUserName()));
        DataNotifyDto notifyDto = new DataNotifyDto()
                .setUsers(userDtos)
                .setTitle(NOTIFY_TITLE)
                .setContent(String.format(MESSAGE_TEMPLATE, msg.getSendUserName(), msg.getFlowTaskName()));
        systemSendNotify.send(notifyDto);

        // 循环发送提醒：false-不循环(只提醒一次)，true-循环发送
        if (Boolean.FALSE.equals(msg.getTimeLimit().getEvent().getLoop())) {
            return;
        }
        // 若循环发送，则重新将消息发布到延迟队列
        Integer mill = timeLimitMessageHandler.calculateMill(msg.getTimeLimit().getEvent().getLoopTime(), TimeLimitTypeEnum.DAY);
        timeLimitMessageHandler.send(mill, msg);
    }
}
