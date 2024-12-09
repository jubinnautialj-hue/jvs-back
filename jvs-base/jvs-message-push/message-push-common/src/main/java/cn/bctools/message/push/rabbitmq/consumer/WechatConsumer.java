package cn.bctools.message.push.rabbitmq.consumer;

import cn.bctools.message.push.dto.enums.MessageTypeEnum;
import cn.bctools.message.push.dto.enums.PlatformEnum;
import cn.bctools.message.push.dto.messagepush.wechatofficialaccount.TemplateMessageDTO;
import cn.bctools.message.push.handler.wechatofficialaccount.TemplateMessageHandler;
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
public class WechatConsumer {

    private final MessagePushHisUtils messagePushHisUtils;
    private final TemplateMessageHandler templateMessageHandler;

    @RabbitListener(queues = RabbitMqUtils.QUEUE_INFORM_WECHAT)
    public void receiveMessage(String message) {
        log.info("-----初始化发送微信模板信息");
        try {
            //转换类
            TemplateMessageDTO templateMessageDTO = JSONUtil.toBean(message, TemplateMessageDTO.class);
            String batchNo = messagePushHisUtils.saveHis(templateMessageDTO, PlatformEnum.WECHAT_OFFICIAL_ACCOUNT, MessageTypeEnum.WECHAT_OFFICIAL_ACCOUNT_TEMPLATE);
            templateMessageHandler.handle(batchNo);
        } catch (Exception e) {
            log.error(" send error", e);
        }
        log.info("-----微信模板信息发送完成");
    }
}
