package cn.bctools.message.push.service;

import cn.bctools.message.push.dto.enums.MessageTypeEnum;

/**
 * @author xh
 */
public interface MessageResendService {
    /**
     * 根据发送消息id 重发
     *
     * @param hisId
     */
    void resendMessage(String hisId);

    /**
     * 根据发送消息id 重发
     *
     * @param messageTypeEnum 发送类型
     * @param hisId
     */
    void resendMessage(String hisId, MessageTypeEnum messageTypeEnum);
}
