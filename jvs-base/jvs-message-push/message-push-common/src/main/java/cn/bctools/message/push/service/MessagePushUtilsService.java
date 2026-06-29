package cn.bctools.message.push.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.message.push.dto.messagepush.MessageBatchSendDto;

/**
 * @author xh
 */
public interface MessagePushUtilsService {

    /**
     * 批量发送消息
     *
     * @param batchSendDto
     * @param pushUser
     */
    void batchSend(MessageBatchSendDto batchSendDto, UserDto pushUser);
}
