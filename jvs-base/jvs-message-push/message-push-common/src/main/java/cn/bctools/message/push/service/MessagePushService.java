package cn.bctools.message.push.service;

import cn.bctools.message.push.dto.messagepush.AliSmsDto;
import cn.bctools.message.push.dto.messagepush.EmailMessageDto;
import cn.bctools.message.push.dto.messagepush.InsideNotificationDto;
import cn.bctools.message.push.dto.messagepush.wechatofficialaccount.TemplateMessageDTO;

/**
 * @author xh
 */
public interface MessagePushService {

    /**
     * 发送邮件消息
     *
     * @param messageDto
     */
    void sendEmailMessage(EmailMessageDto messageDto);

    /**
     * 发送阿里短信消息
     *
     * @param dto
     * @throws Exception 发送可能会失败
     */
    void sendAliSms(AliSmsDto dto) throws Exception;

    /**
     * 发送微信模板消息
     *
     * @param dto
     */
    void sendWechatTemplate(TemplateMessageDTO dto);

    /**
     * 发送站内信消息
     *
     * @param dto
     */
    void sendInside(InsideNotificationDto dto);
}
