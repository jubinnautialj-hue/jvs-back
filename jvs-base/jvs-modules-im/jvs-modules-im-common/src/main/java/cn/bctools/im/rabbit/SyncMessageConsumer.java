package cn.bctools.im.rabbit;

import cn.bctools.im.entity.enums.MessageTypeEnum;
import cn.bctools.im.message.constant.RabbitMqConstant;
import cn.bctools.im.message.dto.SyncChatMessageDto;
import cn.bctools.im.message.dto.SyncNotifyMessageDto;
import cn.bctools.im.service.ImMessageService;
import cn.bctools.im.strategy.SyncMessageStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author ZhuXiaoKang
 * @Description 持久IM消息MQ消费者
 */

@Slf4j
@Component
public class SyncMessageConsumer {

    @Autowired
    private SyncMessageStrategy syncMessageStrategy;

    /**
     * 持久化聊天消息
     *
     * @param syncChatMessageDto 聊天消息
     */
    @RabbitListener(queues = {RabbitMqConstant.QUEUE_SYNC_CHAT})
    public void syncChatMessage(SyncChatMessageDto syncChatMessageDto) {
        if (syncChatMessageDto == null) {
            return;
        }
        // 得到具体的持久化消息服务
        ImMessageService messageService = syncMessageStrategy.getImMessageService(syncChatMessageDto.getChatType());
        if (messageService == null) {
            return;
        }
        // 保存
        messageService.saveMessage(syncChatMessageDto);
    }

    /**
     * 持久化通知消息
     *
     * @param syncNotifyMessageDto 聊天消息
     */
    @RabbitListener(queues = {RabbitMqConstant.QUEUE_SYNC_NOTIFY})
    public void syncNotifyMessage(SyncNotifyMessageDto syncNotifyMessageDto) {
        if (syncNotifyMessageDto == null) {
            return;
        }
        // 得到具体的持久化消息服务
        ImMessageService messageService = syncMessageStrategy.getImMessageService(MessageTypeEnum.NOTIFY.getValue());
        if (messageService == null) {
            return;
        }
        // 保存
        messageService.saveMessage(syncNotifyMessageDto);
    }
}
