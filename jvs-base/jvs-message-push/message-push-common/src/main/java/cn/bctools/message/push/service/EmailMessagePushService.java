package cn.bctools.message.push.service;

import cn.bctools.message.push.dto.messagepush.EmailMessageDto;

/**
 * @author xh
 */
public interface EmailMessagePushService {

    /**
     * 发送邮件消息
     *
     * @param messageDto
     * @param pushUser
     */
    void sendEmailMessage(EmailMessageDto messageDto);

    /**
     * 重发邮件消息
     *
     * @param pushHisId
     */
    void resendEmailMessage(String pushHisId);

}
