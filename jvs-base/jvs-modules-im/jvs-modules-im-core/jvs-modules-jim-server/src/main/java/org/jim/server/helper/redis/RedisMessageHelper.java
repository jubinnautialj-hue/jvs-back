package org.jim.server.helper.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jim.core.ImChannelContext;
import org.jim.core.cache.redis.JedisTemplate;
import org.jim.core.cache.redis.RedisCache;
import org.jim.core.cache.redis.RedisCacheManager;
import org.jim.core.config.ImConfig;
import org.jim.core.enums.ChannelDataBusinessTypeEnum;
import org.jim.core.enums.ChannelDataStoreTypeEnum;
import org.jim.core.listener.ImStoreBindListener;
import org.jim.core.message.AbstractMessageHelper;
import org.jim.core.packets.*;
import org.jim.core.packets.group.GroupUserUpdateReqBody;
import org.jim.core.packets.notify.NotifyReqBody;
import org.jim.core.utils.JsonKit;
import org.jim.server.util.ChatKit;
import org.jim.server.util.RedisKeyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Redis获取持久化+同步消息助手;
 *
 * @author WChao
*/
public class RedisMessageHelper extends AbstractMessageHelper {

    private Logger log = LoggerFactory.getLogger(RedisMessageHelper.class);

    private static final String SUFFIX = ":";


    public RedisMessageHelper() {
        this.imConfig = ImConfig.Global.get();
    }

    @Override
    public ImStoreBindListener getBindListener() {
        return new RedisImStoreBindListener(imConfig, this, null);
    }

    @Override
    public boolean isOnline(String tenantId, String userId) {
        try {
            String keyPattern = RedisKeyUtils.UserCache.userTerminalKey(true, null, tenantId, userId);
            Set<String> terminalKeys = JedisTemplate.me().keys(keyPattern);
            if (CollectionUtils.isEmpty(terminalKeys)) {
                return false;
            }
            Iterator<String> terminalKeyIterator = terminalKeys.iterator();
            while (terminalKeyIterator.hasNext()) {
                String terminalKey = terminalKeyIterator.next();
                terminalKey = terminalKey.substring(terminalKey.indexOf(tenantId + SUFFIX + userId));
                String isOnline = RedisCacheManager.getCache(PREFIX_USER).get(terminalKey, String.class);
                if (UserStatusType.ONLINE.getStatus().equals(isOnline)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return false;
    }

    @Override
    public List<String> getGroupUsers(String tenantId, String groupId, ImChannelContext imChannelContext) {
        String groupUserKey = RedisKeyUtils.GroupCache.groupUserKey(false, imChannelContext.getSessionContext().getTempLinkId(), tenantId, groupId);
        return RedisCacheManager.getCache(PREFIX_GROUP).listGetAll(groupUserKey);
    }


    @Override
    public void writeMessage(String timelineTable, String timelineId, ChatBody chatBody, ImChannelContext imChannelContext) {
        double score = chatBody.getCreateTime();
        RedisCacheManager.getCache(timelineTable).sortSetPush(timelineId, score, chatBody);
    }

    @Override
    public void writeMessage(Message message, ImChannelContext imChannelContext) {

    }

    @Override
    public void addGroupUser(String tenantId, String userId, String groupId) {
        String key = tenantId + SUFFIX + groupId;
        List<String> users = RedisCacheManager.getCache(PREFIX_GROUP).listGetAll(key);
        if (users.contains(userId)) {
            return;
        }
        RedisCacheManager.getCache(PREFIX_GROUP).listPushTail(key, userId);
    }

    @Override
    public void removeGroupUser(String tenantId, String userId, String groupId) {
        String key = tenantId + SUFFIX + groupId;
        RedisCacheManager.getCache(PREFIX_GROUP).listRemove(key, userId);
    }

    @Override
    public UserMessageData getFriendsOfflineMessage(String tenantId, String userId, String fromUserId) {
        String userFriendKey = RedisKeyUtils.PushCache.friendUserOfflineKey(false, tenantId, userId, fromUserId);
        List<String> messageList = RedisCacheManager.getCache(PREFIX_PUSH).sortSetGetAll(userFriendKey);
        List<ChatBody> messageDataList = JsonKit.toArray(messageList, ChatBody.class);
        RedisCacheManager.getCache(PREFIX_PUSH).remove(userFriendKey);
        return putFriendsMessage(new UserMessageData(userId), messageDataList, null);
    }

    @Override
    public UserMessageData getFriendsOfflineMessage(String tenantId, String userId) {
        UserMessageData messageData = new UserMessageData(userId);
        try {
            Set<String> userKeys = JedisTemplate.me().keys(RedisKeyUtils.PushCache.friendUserOfflineKey(true, tenantId, userId, null));
            //获取好友离线消息;
            if (CollectionUtils.isNotEmpty(userKeys)) {
                List<ChatBody> messageList = new ArrayList<ChatBody>();
                Iterator<String> userKeyIterator = userKeys.iterator();
                while (userKeyIterator.hasNext()) {
                    String userKey = userKeyIterator.next();
                    userKey = userKey.substring(userKey.indexOf(USER + SUFFIX + tenantId + SUFFIX));
                    List<String> messages = RedisCacheManager.getCache(PREFIX_PUSH).sortSetGetAll(userKey);
                    RedisCacheManager.getCache(PREFIX_PUSH).remove(userKey);
                    messageList.addAll(JsonKit.toArray(messages, ChatBody.class));
                }
                putFriendsMessage(messageData, messageList, null);
            }

            //获取群组离线消息;
            List<String> groupIdList = RedisCacheManager.getCache(PREFIX_USER).listGetAll(RedisKeyUtils.UserCache.userGroupKey(false, tenantId, userId));
            if (CollectionUtils.isNotEmpty(groupIdList)) {
                groupIdList.forEach(groupId -> {
                    UserMessageData groupMessageData = getGroupOfflineMessage(tenantId, userId, groupId);
                    if (Objects.isNull(groupMessageData)) {
                        return;
                    }
                    putGroupMessage(messageData, groupMessageData.getGroups().get(groupId));
                });
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return messageData;
    }

    @Override
    public UserMessageData getNotifyOfflineMessage(String tenantId, String userId) {
        UserMessageData messageData = new UserMessageData(userId);
        try {
            // 获取通知离线消息
            Set<String> notifyKeys = JedisTemplate.me().keys(RedisKeyUtils.PushCache.notifyUserOfflineKey(true, tenantId, userId));
            if (CollectionUtils.isNotEmpty(notifyKeys)) {
                List<NotifyReqBody> messageList = new ArrayList<NotifyReqBody>();
                Iterator<String> notifyKeyIterator = notifyKeys.iterator();
                while (notifyKeyIterator.hasNext()) {
                    String notifyKey = notifyKeyIterator.next();
                    notifyKey = notifyKey.substring(notifyKey.indexOf(NOTIFY + SUFFIX + tenantId + SUFFIX));
                    List<String> messages = RedisCacheManager.getCache(PREFIX_PUSH).sortSetGetAll(notifyKey);
                    RedisCacheManager.getCache(PREFIX_PUSH).remove(notifyKey);
                    messageList.addAll(JsonKit.toArray(messages, NotifyReqBody.class));
                }
                messageData.setNotifys(messageList);
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return messageData;
    }

    @Override
    public UserMessageData getGroupOfflineMessage(String tenantId, String userId, String groupId) {
        UserMessageData messageData = new UserMessageData(userId);
        String userGroupKey = RedisKeyUtils.PushCache.groupUserOfflineKey(false, tenantId, groupId, userId);
        List<String> messages = RedisCacheManager.getCache(PREFIX_PUSH).sortSetGetAll(userGroupKey);
        if (CollectionUtils.isEmpty(messages)) {
            return messageData;
        }
        putGroupMessage(messageData, JsonKit.toArray(messages, ChatBody.class));
        RedisCacheManager.getCache(PREFIX_PUSH).remove(userGroupKey);
        return messageData;
    }

    @Override
    public UserMessageData getFriendHistoryMessage(String tenantId, String userId, String fromUserId, Long beginTime, Long endTime, Long offset, Long count) {
        String sessionId = ChatKit.sessionId(userId, fromUserId);
        String userSessionKey = USER + SUFFIX + tenantId + SUFFIX + sessionId;
        List<String> messages = getHistoryMessage(userSessionKey, beginTime, endTime, offset, count);
        UserMessageData messageData = new UserMessageData(userId);
        putFriendsMessage(messageData, JsonKit.toArray(messages, ChatBody.class), fromUserId);
        return messageData;
    }

    @Override
    public UserMessageData getGroupHistoryMessage(String tenantId, String userId, String groupId, Long beginTime, Long endTime, Long offset, Long count) {
        String groupKey = GROUP + SUFFIX + tenantId + SUFFIX + groupId;
        List<String> messages = getHistoryMessage(groupKey, beginTime, endTime, offset, count);
        UserMessageData messageData = new UserMessageData(userId);
        putGroupMessage(messageData, JsonKit.toArray(messages, ChatBody.class));
        return messageData;
    }

    @Override
    public UserMessageData getNotifyHistoryMessage(String tenantId, String userId, Long beginTime, Long endTime, Long offset, Long count) {
        return null;
    }

    private List<String> getHistoryMessage(String historyKey, Long beginTime, Long endTime, Long offset, Long count) {
        boolean isTimeBetween = (beginTime != null && endTime != null);
        boolean isPage = (offset != null && count != null);
        RedisCache storeCache = RedisCacheManager.getCache(PREFIX_STORE);
        //消息区间，不分页
        if (isTimeBetween && !isPage) {
            return storeCache.sortSetGetAll(historyKey, beginTime, endTime);
            //消息区间，并且分页;
        } else if (isTimeBetween && isPage) {
            return storeCache.sortSetGetAll(historyKey, beginTime, endTime, offset.intValue(), count.intValue());
            //所有消息，并且分页;
        } else if (isPage) {
            return storeCache.sortSetGetAll(historyKey, 0, Double.MAX_VALUE, offset.intValue(), count.intValue());
            //所有消息，不分页;
        } else {
            return storeCache.sortSetGetAll(historyKey);
        }
    }

    /**
     * 放入用户群组消息;
     *
     * @param userMessage
     * @param messages
     */
    public UserMessageData putGroupMessage(UserMessageData userMessage, List<ChatBody> messages) {
        if (Objects.isNull(userMessage) || CollectionUtils.isEmpty(messages)) {
            return userMessage;
        }
        messages.forEach(chatBody -> {
            String groupId = chatBody.getGroupId();
            if (StringUtils.isEmpty(groupId)) {
                return;
            }
            List<ChatBody> groupMessages = userMessage.getGroups().get(groupId);
            if (CollectionUtils.isEmpty(groupMessages)) {
                groupMessages = new ArrayList();
                userMessage.getGroups().put(groupId, groupMessages);
            }
            groupMessages.add(chatBody);
        });
        return userMessage;
    }

    /**
     * 组装放入用户好友消息;
     *
     * @param userMessage
     * @param messages
     */
    public UserMessageData putFriendsMessage(UserMessageData userMessage, List<ChatBody> messages, String friendId) {
        if (Objects.isNull(userMessage) || CollectionUtils.isEmpty(messages)) {
            return userMessage;
        }
        messages.forEach(chatBody -> {
            String fromId = chatBody.getFrom();
            if (StringUtils.isEmpty(fromId)) {
                return;
            }
            String targetFriendId = friendId;
            if (StringUtils.isEmpty(targetFriendId)) {
                targetFriendId = fromId;
            }
            List<ChatBody> friendMessages = userMessage.getFriends().get(targetFriendId);
            if (CollectionUtils.isEmpty(friendMessages)) {
                friendMessages = new ArrayList();
                userMessage.getFriends().put(targetFriendId, friendMessages);
            }
            friendMessages.add(chatBody);
        });
        return userMessage;
    }

    /**
     * 获取群组所有成员信息
     *
     * @param groupId                               群组ID
     * @param type(0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线])
     * @return
     */
    @Override
    public Group getGroupUsers(String tenantId, String groupId, Integer type, ImChannelContext imChannelContext) {
        if (Objects.isNull(groupId) || Objects.isNull(type)) {
            log.warn("group:{} or type:{} is null", groupId, type);
            return null;
        }
        Group group = RedisCacheManager.getCache(PREFIX_GROUP).get(RedisKeyUtils.GroupCache.groupInfoKey(false, tenantId, groupId), Group.class);
        if (Objects.isNull(group)) {
            return null;
        }

        Map<Object, Object> userMap =  RedisCacheManager.getCache(PREFIX_GROUP).hGetAll(RedisKeyUtils.GroupCache.groupUserInfoKey(false, imChannelContext.getSessionContext().getTempLinkId(), tenantId, groupId));
        if (MapUtils.isEmpty(userMap)) {
            return null;
        }
        List<User> users = new ArrayList<User>();
        for (Map.Entry<Object, Object> u : userMap.entrySet()) {
            User us = JSON.parseObject(u.getValue().toString(), User.class);
            User user = getUserStatus(tenantId, us);
            if (Objects.isNull(user)) {
                continue;
            }
            validateStatusByType(type, users, user);
        }

        // 排序，online在前
        users = users.stream().sorted(Comparator.comparing(User::getStatus).reversed()).collect(Collectors.toList());
        // 得到在线用户数量
        Long onlineCount = users.stream().filter(user -> UserStatusType.ONLINE.getStatus().equals(user.getStatus())).count();

        group.setOnline(onlineCount.intValue());
        group.setUsers(users);

        return group;
    }

    /**
     * 根据获取type校验是否组装User
     *
     * @param type
     * @param users
     * @param user
     */
    private void validateStatusByType(Integer type, List<User> users, User user) {
        String status = user.getStatus();
        if (UserStatusType.ONLINE.getNumber() == type && UserStatusType.ONLINE.getStatus().equals(status)) {
            users.add(user);
        } else if (UserStatusType.OFFLINE.getNumber() == type && UserStatusType.OFFLINE.getStatus().equals(status)) {
            users.add(user);
        } else if (UserStatusType.ALL.getNumber() == type) {
            users.add(user);
        }
    }

    /**
     * 用户在线状态
     *
     * @param tenantId
     * @param user
     * @return
     */
    public User getUserStatus(String tenantId, User user) {
        if (Objects.isNull(user)) {
            return null;
        }
        boolean isOnline = this.isOnline(tenantId, user.getUserId());
        String status = isOnline ? UserStatusType.ONLINE.getStatus() : UserStatusType.OFFLINE.getStatus();
        user.setStatus(status);
        return user;
    }

    @Override
    public User getUserByType(String tenantId, String userId, Integer type) {
        User user = RedisCacheManager.getCache(PREFIX_USER).get(RedisKeyUtils.UserCache.userInfoKey(false, tenantId, userId), User.class);
        if (Objects.isNull(user)) {
            return null;
        }
        boolean isOnline = this.isOnline(tenantId, userId);
        String status = isOnline ? UserStatusType.ONLINE.getStatus() : UserStatusType.OFFLINE.getStatus();
        if (UserStatusType.ONLINE.getNumber() == type && isOnline) {
            user.setStatus(status);
            return user;
        } else if (UserStatusType.OFFLINE.getNumber() == type && !isOnline) {
            user.setStatus(status);
            return user;
        } else if (type == UserStatusType.ALL.getNumber()) {
            user.setStatus(status);
            return user;
        }
        return null;
    }

    @Override
    public Group getFriendUsers(String tenantId, String userId, String friendGroupId, Integer type) {
        boolean isTrue = Objects.isNull(userId) || Objects.isNull(friendGroupId) || Objects.isNull(type);
        if (isTrue) {
            log.warn("userId:{} or friendGroupId:{} or type:{} is null");
            return null;
        }
        List<Group> friends = RedisCacheManager.getCache(PREFIX_USER).get(RedisKeyUtils.UserCache.userFriendKey(false, tenantId, userId), List.class);
        if (CollectionUtils.isEmpty(friends)) {
            return null;
        }
        for (Group group : friends) {
            if (!friendGroupId.equals(group.getGroupId())) {
                continue;
            }
            List<User> users = group.getUsers();
            if (CollectionUtils.isEmpty(users)) {
                return group;
            }
            List<User> userResults = new ArrayList<User>();
            for (User user : users) {
                initUserStatus(user);
                validateStatusByType(type, userResults, user);
            }
            group.setUsers(userResults);
            return group;
        }
        return null;
    }

    /**
     * 初始化用户在线状态;
     *
     * @param user
     */
    public boolean initUserStatus(User user) {
        if (Objects.isNull(user) || Objects.isNull(user.getUserId())) {
            return false;
        }
        String userId = user.getUserId();
        boolean isOnline = this.isOnline(user.getTenantId(), userId);
        if (isOnline) {
            user.setStatus(UserStatusType.ONLINE.getStatus());
        } else {
            user.setStatus(UserStatusType.OFFLINE.getStatus());
        }
        return true;
    }

    /**
     * 获取好友分组所有成员信息
     *
     * @param userId                                用户ID
     * @param type(0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线])
     * @return
     */
    @Override
    public List<Group> getAllFriendUsers(String tenantId, String userId, Integer type) {
        if (Objects.isNull(userId)) {
            return null;
        }
        List<JSONObject> friendJsonArray = RedisCacheManager.getCache(PREFIX_USER).get(RedisKeyUtils.UserCache.userFriendKey(false, tenantId, userId), List.class);
        if (CollectionUtils.isEmpty(friendJsonArray)) {
            return null;
        }
        List<Group> friends = new ArrayList<Group>();
        friendJsonArray.forEach(groupJson -> {
            Group group = JSONObject.toJavaObject(groupJson, Group.class);
            List<User> users = group.getUsers();
            if (CollectionUtils.isEmpty(users)) {
                return;
            }
            List<User> userResults = new ArrayList<User>();
            for (User user : users) {
                initUserStatus(user);
                validateStatusByType(type, userResults, user);
            }
            group.setUsers(userResults);
            friends.add(group);
        });
        return friends;
    }

    @Override
    public List<Group> getAllGroupUsers(String tenantId, String userId, Integer type, ImChannelContext imChannelContext) {
        if (Objects.isNull(userId)) {
            return null;
        }
        List<String> groupIds = RedisCacheManager.getCache(PREFIX_USER).listGetAll(RedisKeyUtils.UserCache.userGroupKey(false, tenantId, userId));
        if (CollectionUtils.isEmpty(groupIds)) {
            return null;
        }
        List<Group> groups = new ArrayList<Group>();
        groupIds.forEach(groupId -> {
            Group group = getGroupUsers(tenantId, groupId, type, imChannelContext);
            if (Objects.isNull(group)) {
                return;
            }
            groups.add(group);
        });
        return groups;
    }

    /**
     * 更新用户终端协议类型及在线状态;
     *
     * @param user 用户信息
     */
    @Override
    public boolean updateUserTerminal(User user) {
        String userId = user.getUserId();
        String terminal = user.getTerminal();
        String status = user.getStatus();
        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(terminal) || StringUtils.isEmpty(status)) {
            log.error("userId:{},terminal:{},status:{} must not null", userId, terminal, status);
            return false;
        }
        RedisCacheManager.getCache(PREFIX_USER).put(RedisKeyUtils.UserCache.userTerminalKey(false, terminal, user.getTenantId(), userId), user.getStatus());
        RedisCacheManager.getCache(PREFIX_USER).put(RedisKeyUtils.UserCache.userInfoKey(false, user.getTenantId(), userId), user.clone());


        return true;
    }

    /**
     * 获取用户拥有的群组;
     *
     * @param userId
     * @return
     */
    @Override
    public List<String> getGroups(String tenantId, String userId) {
        return RedisCacheManager.getCache(PREFIX_USER).listGetAll(RedisKeyUtils.UserCache.userGroupKey(false, tenantId, userId));
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

    static {
        RedisCacheManager.register(PREFIX_USER, Integer.MAX_VALUE, Integer.MAX_VALUE);
        RedisCacheManager.register(PREFIX_GROUP, Integer.MAX_VALUE, Integer.MAX_VALUE);
        RedisCacheManager.register(PREFIX_STORE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        RedisCacheManager.register(PREFIX_PUSH, Integer.MAX_VALUE, Integer.MAX_VALUE);
        RedisCacheManager.register(PREFIX_BUSINESS, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

}
