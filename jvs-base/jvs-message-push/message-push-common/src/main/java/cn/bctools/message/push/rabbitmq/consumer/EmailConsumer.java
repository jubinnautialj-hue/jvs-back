package cn.bctools.message.push.rabbitmq.consumer;

import cn.bctools.message.push.dto.enums.MessageTypeEnum;
import cn.bctools.message.push.dto.enums.PlatformEnum;
import cn.bctools.message.push.dto.messagepush.EmailMessageDto;
import cn.bctools.message.push.handler.EmailMessageHandler;
import cn.bctools.message.push.utils.MessagePushHisUtils;
import cn.bctools.message.push.utils.RabbitMqUtils;
import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author wl
 */
@Slf4j
@Component
@Lazy(false)
@AllArgsConstructor
public class EmailConsumer {

    private final MessagePushHisUtils messagePushHisUtils;
    private final EmailMessageHandler emailMessageHandler;

    @RabbitListener(queues = RabbitMqUtils.QUEUE_INFORM_EMAIL)
    public void receiveMessage(String message) {
        log.info("-----初始化发送邮件");
        try {
            //转换类
            EmailMessageDto email = JSONUtil.toBean(message, EmailMessageDto.class);
            String batchNo = messagePushHisUtils.saveHis(email, PlatformEnum.EMAIL, MessageTypeEnum.EMAIL);
            emailMessageHandler.handle(batchNo);
        } catch (Exception e) {
            log.error(" send error", e);
        }
        log.info("-----邮件发送完成");
    }
}
