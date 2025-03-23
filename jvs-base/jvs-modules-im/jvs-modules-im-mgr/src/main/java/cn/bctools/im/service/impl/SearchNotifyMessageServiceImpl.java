package cn.bctools.im.service.impl;

import cn.bctools.im.service.SearchMessageService;
import org.jim.core.message.MessageHelper;
import org.jim.core.packets.UserMessageData;
import org.jim.core.packets.message.MessageReqBody;
import org.jim.core.packets.message.MessageReqTypeEnum;
import org.springframework.stereotype.Service;

/**
 * @Author: ZhuXiaoKang
 * @Description: 通知消息查询
 */
@Service
public class SearchNotifyMessageServiceImpl implements SearchMessageService {

    @Override
    public String getType() {
       return MessageReqTypeEnum.NOTIFY_OFFLINE.name() + MessageReqTypeEnum.NOTIFY_HISTORY.name();
    }

    @Override
    public UserMessageData search(MessageReqBody messageReqBody, MessageHelper messageHelper) {
        // 获取与指定用户所有离线通知消息
        if (MessageReqTypeEnum.NOTIFY_OFFLINE.getValue() == messageReqBody.getMessageType()) {
            return messageHelper.getNotifyOfflineMessage(messageReqBody.getTenantId(), messageReqBody.getUserId());
        }
        // 分页获取与指定用户历史通知消息
        if (MessageReqTypeEnum.NOTIFY_HISTORY.getValue() == messageReqBody.getMessageType()) {
            return messageHelper.getNotifyHistoryMessage(messageReqBody.getTenantId(), messageReqBody.getUserId()
                    ,messageReqBody.getBeginTime(), messageReqBody.getEndTime(), messageReqBody.getOffset()
                    ,messageReqBody.getCount());
        }
        return null;
    }
}
