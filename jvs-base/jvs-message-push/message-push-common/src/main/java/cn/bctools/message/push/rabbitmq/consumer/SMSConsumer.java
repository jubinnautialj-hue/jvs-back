package cn.bctools.message.push.rabbitmq.consumer;

import cn.bctools.message.push.dto.enums.MessageTypeEnum;
import cn.bctools.message.push.dto.enums.PlatformEnum;
import cn.bctools.message.push.dto.messagepush.AliSmsDto;
import cn.bctools.message.push.handler.AliSmsHandler;
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
public class SMSConsumer {

    private final MessagePushHisUtils messagePushHisUtils;
    private final AliSmsHandler aliSmsHandler;


    @RabbitListener(queues = RabbitMqUtils.QUEUE_INFORM_SMS)
    public void receiveMessage(String message) {
        log.info("-----初始化发送短信");
        try {
            //转换类
            AliSmsDto aliSmsDto = JSONUtil.toBean(message, AliSmsDto.class);
            String batchNo = messagePushHisUtils.saveHis(aliSmsDto, PlatformEnum.ALI_SMS, MessageTypeEnum.ALI_SMS);
            aliSmsHandler.handle(batchNo);
        } catch (Exception e) {
            log.error(" send error", e);
        }
        log.info("-----短信发送完成");
    }
}
