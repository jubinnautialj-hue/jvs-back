package cn.bctools.message.push.service;

import cn.bctools.message.push.dto.messagepush.InsideNotificationDto;

/**
 * @author czy
 */
public interface InsideNotificationService {

    /**
     * 站内
     *
     * @param pushUser 发送用户
     * @param dto      数据
     */
    void send(InsideNotificationDto dto);

    /**
     * 重新发送
     *
     * @param pushHisId 发送历史
     */
    void resend(String pushHisId);
}
