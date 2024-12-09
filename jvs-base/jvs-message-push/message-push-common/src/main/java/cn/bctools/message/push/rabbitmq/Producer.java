package cn.bctools.message.push.rabbitmq;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.message.push.dto.messagepush.AliSmsDto;
import cn.bctools.message.push.dto.messagepush.BaseMessage;
import cn.bctools.message.push.dto.messagepush.EmailMessageDto;
import cn.bctools.message.push.dto.messagepush.InsideNotificationDto;
import cn.bctools.message.push.dto.messagepush.dingtalk.corp.TextMessageDTO;
import cn.bctools.message.push.dto.messagepush.wechatofficialaccount.TemplateMessageDTO;
import cn.bctools.message.push.entity.MessagePushLog;
import cn.bctools.message.push.service.MessagePushLogService;
import cn.bctools.message.push.utils.RabbitMqUtils;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * @author wl
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class Producer<T extends BaseMessage> {

    private final RabbitTemplate rabbitTemplate;

    private final MessagePushLogService messagePushLogService;

    public void push2Mq(T dto) {
        try {
            saveLog(dto);
            Class<? extends BaseMessage> aClass = dto.getClass();
            //短信
            if(AliSmsDto.class.equals(aClass)){
                rabbitTemplate.convertAndSend(RabbitMqUtils.QUEUE_INFORM_SMS, JSONUtil.toJsonStr(dto));
                return;
            }
            //邮件
            if(EmailMessageDto.class.equals(aClass)){
                rabbitTemplate.convertAndSend(RabbitMqUtils.QUEUE_INFORM_EMAIL, JSONUtil.toJsonStr(dto));
                return;
            }
            //站内信
            if(InsideNotificationDto.class.equals(aClass)){
                rabbitTemplate.convertAndSend(RabbitMqUtils.QUEUE_INFORM_INSIDE, dto);
                return;
            }
            //微信公众号
            if(TemplateMessageDTO.class.equals(aClass)){
                rabbitTemplate.convertAndSend(RabbitMqUtils.QUEUE_INFORM_WECHAT, JSONUtil.toJsonStr(dto));
                return;
            }
            //短信
            if(TextMessageDTO.class.equals(aClass)){
                rabbitTemplate.convertAndSend(RabbitMqUtils.QUEUE_INFORM_DING_H5, JSONUtil.toJsonStr(dto));
            }
        } catch (Exception e) {
            log.error("消息类型匹配错误", e);
        }
    }

    private void saveLog(T dto){
        MessagePushLog copy = BeanCopyUtil.copy(dto, MessagePushLog.class);
        copy.setExpandData(JSONUtil.toJsonStr(dto));
        messagePushLogService.save(copy);
    }
}
