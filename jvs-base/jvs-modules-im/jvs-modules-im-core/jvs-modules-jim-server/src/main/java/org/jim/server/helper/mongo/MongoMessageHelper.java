package org.jim.server.helper.mongo;

import org.jim.core.ImChannelContext;
import org.jim.core.enums.ChannelDataBusinessTypeEnum;
import org.jim.core.enums.ChannelDataStoreTypeEnum;
import org.jim.core.listener.ImStoreBindListener;
import org.jim.core.message.MessageHelper;
import org.jim.core.packets.*;
import org.jim.core.packets.group.GroupUserUpdateReqBody;

import java.util.List;

/**
 * @author WChao
 * @Desc Mongo获取持久化+同步消息助手;
*/
public class MongoMessageHelper implements MessageHelper {
    @Override
    public ImStoreBindListener getBindListener() {
        return null;
    }

    @Override
    public boolean isOnline(String tenantId, String userId) {
        return false;
    }

    @Override
    public Group getGroupUsers(String tenantId, String group_id, Integer type, ImChannelContext imChannelContext) {
        return null;
    }

    @Override
    public List<Group> getAllGroupUsers(String tenantId, String user_id, Integer type, ImChannelContext imChannelContext) {
        return null;
    }

    @Override
    public Group getFriendUsers(String tenantId, String user_id, String friend_group_id, Integer type) {
        return null;
    }

    @Override
    public List<Group> getAllFriendUsers(String tenantId, String user_id, Integer type) {
        return null;
    }

    @Override
    public User getUserByType(String tenantId, String userid, Integer type) {
        return null;
    }

    @Override
    public void addGroupUser(String tenantId, String userid, String group_id) {

    }

    @Override
    public List<String> getGroupUsers(String tenantId, String group_id, ImChannelContext imChannelContext) {
        return null;
    }

    @Override
    public List<String> getGroups(String tenantId, String user_id) {
        return null;
    }

    @Override
    public void writeMessage(String timelineTable, String timelineId, ChatBody chatBody, ImChannelContext imChannelContext) {

    }

    @Override
    public void writeMessage(Message message, ImChannelContext imChannelContext) {

    }

    @Override
    public void removeGroupUser(String tenantId, String userId, String group_id) {

    }

    @Override
    public UserMessageData getFriendsOfflineMessage(String tenantId, String userId, String fromUserId) {
        return null;
    }

    @Override
    public UserMessageData getFriendsOfflineMessage(String tenantId, String userId) {
        return null;
    }

    @Override
    public UserMessageData getNotifyOfflineMessage(String tenantId, String userId) {
        return null;
    }

    @Override
    public UserMessageData getGroupOfflineMessage(String tenantId, String userId, String groupId) {
        return null;
    }

    @Override
    public UserMessageData getFriendHistoryMessage(String tenantId, String userId, String fromUserId, Long beginTime, Long endTime, Long offset, Long count) {
        return null;
    }

    @Override
    public UserMessageData getGroupHistoryMessage(String tenantId, String userId, String groupId, Long beginTime, Long endTime, Long offset, Long count) {
        return null;
    }

    @Override
    public UserMessageData getNotifyHistoryMessage(String tenantId, String userId, Long beginTime, Long endTime, Long offset, Long count) {
        return null;
    }

    @Override
    public boolean updateUserTerminal(User user) {
        return false;
    }

    @Override
    public UserMessageData getBusinessMessage(String tenantId, String businessId) {
        return null;
    }

    @Override
    public void updateGroupUser(String userId, GroupUserUpdateReqBody reqBody, ImChannelContext imChannelContext) {

    }

    @Override
    public void saveChannelData(String userId, ChannelDataStoreTypeEnum storeTypeEnum, ChannelDataBusinessTypeEnum businessTypeEnum, String storeCode, Object expand) {

    }

    @Override
    public void deleteChannelData(String userId, ChannelDataBusinessTypeEnum businessTypeEnum, String storeCode) {

    }
}
