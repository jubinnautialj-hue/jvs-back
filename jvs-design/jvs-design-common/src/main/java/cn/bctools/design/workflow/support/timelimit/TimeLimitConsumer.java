package cn.bctools.design.workflow.support.timelimit;

import cn.bctools.design.workflow.entity.FlowTaskPerson;
import cn.bctools.design.workflow.entity.enums.ProcessStatusEnum;
import cn.bctools.design.workflow.service.FlowTaskPersonService;
import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * @author zhuxiaokang
 * 消费审批期限超时校验消息
 */
@Slf4j
@Component
@AllArgsConstructor
public class TimeLimitConsumer {

    private final FlowTaskPersonService flowTaskPersonService;
    private final CompositeOverdueHandler compositeOverdueHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 审批期限超时校验消息消费者
     *
     * @return
     */
    @Bean(TimeLimitMqConfig.TIME_LIMIT_FUNCTION)
    public Consumer<Message<byte[]>> flowApprovalTimeout() {
        return message -> {
            TimeLimitMessageDto messageDto = null;
            try {
                byte[] payload = message.getPayload();
                messageDto = objectMapper.readValue(payload, TimeLimitMessageDto.class);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            if (messageDto != null) {
                process(messageDto);
            }
        };
    }

    /**
     * 处理
     *
     * @param msg 消息
     */
    private void process(TimeLimitMessageDto msg) {
        FlowTaskPerson flowTaskPerson = flowTaskPersonService.getById(msg.getFlowTaskPersonId());
        if (ObjectUtil.isNull(flowTaskPerson)) {
            return;
        }
        // 审批状态已不是待处理，则直接丢弃消息
        if (Boolean.FALSE.equals(ProcessStatusEnum.PENDING.equals(flowTaskPerson.getProcessStatus()))) {
            return;
        }
        // 审批状态还是待处理，则根据配置处理“审批期限超时后执行策略”
        try {
            compositeOverdueHandler.overdueProcess(msg, flowTaskPerson);
        } catch (Exception e) {
            log.error("审批超时处理消费异常：{}", e.getMessage());
        }
    }
}
