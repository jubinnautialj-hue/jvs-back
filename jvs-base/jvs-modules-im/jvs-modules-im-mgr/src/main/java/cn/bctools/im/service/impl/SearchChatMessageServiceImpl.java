package cn.bctools.im.service.impl;

import cn.bctools.im.service.SearchMessageService;
import org.apache.commons.lang3.StringUtils;
import org.jim.core.message.MessageHelper;
import org.jim.core.packets.UserMessageData;
import org.jim.core.packets.message.MessageReqBody;
import org.jim.core.packets.message.MessageReqTypeEnum;
import org.springframework.stereotype.Service;

/**
 * @Author: ZhuXiaoKang
 * @Description: 聊天消息查询服务
 */
@Service
public class SearchChatMessageServiceImpl implements SearchMessageService {

    @Override
    public String getType() {
         return MessageReqTypeEnum.CHAT_OFFLINE.name() + MessageReqTypeEnum.CHAT_HISTORY.name();
    }

    @Override
    public UserMessageData search(MessageReqBody messageReqBody, MessageHelper messageHelper) {
        // 群组ID
        String groupId = messageReqBody.getGroupId();
        // 当前用户ID
        String userId = messageReqBody.getUserId();
        // 消息来源用户ID
        String fromUserId = messageReqBody.getFromUserId();
        // 消息区间开始时间
        Long beginTime = messageReqBody.getBeginTime();
        // 消息区间结束时间
        Long endTime = messageReqBody.getEndTime();
        // 分页偏移量
        Long offset = messageReqBody.getOffset();
        // 分页数量
        Long count = messageReqBody.getCount();
        // 消息类型
        int type = messageReqBody.getMessageType();
        // 租户id
        String tenantId = messageReqBody.getTenantId();

        // 查询离线消息
        if (MessageReqTypeEnum.CHAT_OFFLINE.getValue() == type) {
            // 群组id不为空，则取用户该群组消息
            if(!StringUtils.isEmpty(groupId)){
                return messageHelper.getGroupOfflineMessage(tenantId,userId,groupId);
            }
            // 获取用户所有离线消息(好友+群组)
            if(StringUtils.isEmpty(fromUserId)){
                return messageHelper.getFriendsOfflineMessage(tenantId,userId);
            }
            // 获取与指定用户离线消息
            return messageHelper.getFriendsOfflineMessage(tenantId, userId, fromUserId);
        }

        // 查询历史消息
        if (MessageReqTypeEnum.CHAT_HISTORY.getValue() == type) {
            // 群组id不为空，则取用户该群组消息
            if(!StringUtils.isEmpty(groupId)){
                return messageHelper.getGroupHistoryMessage(tenantId, userId, groupId,beginTime,endTime,offset,count);
            }
            // 获取与指定用户历史消息
            if(StringUtils.isNotEmpty(fromUserId)){
                return messageHelper.getFriendHistoryMessage(tenantId, userId, fromUserId,beginTime,endTime,offset,count);
            }
        }
        return null;
    }

}
