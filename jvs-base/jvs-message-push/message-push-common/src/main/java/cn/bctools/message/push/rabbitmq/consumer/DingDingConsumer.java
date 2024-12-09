package cn.bctools.message.push.rabbitmq.consumer;

import cn.bctools.message.push.dto.enums.MessageTypeEnum;
import cn.bctools.message.push.dto.enums.PlatformEnum;
import cn.bctools.message.push.dto.messagepush.EmailMessageDto;
import cn.bctools.message.push.dto.messagepush.dingtalk.corp.TextMessageDTO;
import cn.bctools.message.push.handler.EmailMessageHandler;
import cn.bctools.message.push.handler.dingtalk.corp.CorpTextMessageHandler;
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
public class DingDingConsumer {

    private final MessagePushHisUtils messagePushHisUtils;
    private final CorpTextMessageHandler corpTextMessageHandler;

    @RabbitListener(queues = RabbitMqUtils.QUEUE_INFORM_DING_H5)
    public void receiveMessage(String message) {
        log.info("-----初始化发送钉钉消息");
        try {
            //转换类
            TextMessageDTO textMessageDTO = JSONUtil.toBean(message, TextMessageDTO.class);
            String batchNo = messagePushHisUtils.saveHis(textMessageDTO, PlatformEnum.DING_TALK_CORP, MessageTypeEnum.DING_TALK_COPR_TEXT);
            corpTextMessageHandler.handle(batchNo);
        } catch (Exception e) {
            log.error(" send error", e);
        }
        log.info("-----钉钉消息发送完成");
    }
}
