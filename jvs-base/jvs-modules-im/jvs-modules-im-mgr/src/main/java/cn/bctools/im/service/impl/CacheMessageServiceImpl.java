package cn.bctools.im.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.im.service.CacheMessageService;
import cn.bctools.im.service.SocialService;
import cn.bctools.im.service.UpmsUserService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jim.core.ImChannelContext;
import org.jim.core.ImConst;
import org.jim.core.cache.redis.RedisCache;
import org.jim.core.cache.redis.RedisCacheManager;
import org.jim.core.config.ImConfig;
import org.jim.core.packets.*;
import org.jim.core.packets.group.GroupUserUpdateReqBody;
import org.jim.core.packets.notify.NotifyReqBody;
import org.jim.core.packets.notify.NotifyTypeEnum;
import org.jim.core.utils.JsonKit;
import org.jim.server.config.ImServerConfig;
import org.jim.server.helper.redis.RedisMessageHelper;
import org.jim.server.util.ChatKit;
import org.jim.server.util.NotifyKit;
import org.jim.server.util.RedisKeyUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ZhuXiaoKang
 * @Description 缓存处理
 * 缓存key说明：
 *   1. 用户离线消息：  push:user:目标用户id:发送用户id
 *   2. 群组离线消息：  push:group:群组id:用户id
 *   3. 通知离线消息：  push:notify:目标用户id
 *
 */
@Slf4j
@Service
public class CacheMessageServiceImpl implements CacheMessageService {

    /**
     * 缓存帮助类
     */
    private RedisMessageHelper cacheMessageHelper;

    @Resource
    private SocialService socialService;

    @Resource
    private UpmsUserService userService;

    public CacheMessageServiceImpl() {
        // 使用JIM提供的RedisMessageHelper， 若有需要也可替换成自定义的缓存帮助类
        cacheMessageHelper = new RedisMessageHelper();
    }

    @Override
    public void saveMessage(Message message, ImChannelContext imChannelContext) {
        // 聊天离线消息保存到redis
        if (Command.COMMAND_CHAT_REQ.getNumber() == message.getCmd().intValue()) {
            saveChatOffline(message, imChannelContext);
        }
        // 通知离线消息保存到redis
        if (Command.COMMAND_NOTIFY_REQ.getNumber() == message.getCmd().intValue()) {
            saveNotifyOffline(message, imChannelContext);
        }
        // 业务自定义消息保存到redis
        if (Command.COMMAND_BUSINESS_REQ.getNumber() == message.getCmd().intValue()) {
            saveBusinessMessage(message);
        }

    }

    /**
     * 保存聊天离线消息到缓存
     *
     * @param message
     */
    private void saveChatOffline(Message message, ImChannelContext imChannelContext) {
        ChatBody chatBody = (ChatBody)message;
        // 存储群聊离线消息
        if (ChatType.CHAT_TYPE_PUBLIC.getNumber() == chatBody.getChatType()) {
            pushGroupMessages(chatBody.getGroupId(), message, imChannelContext);
        }

        // 存储私聊离线消息
        if (ChatType.CHAT_TYPE_PRIVATE.getNumber() == chatBody.getChatType()){
            String to = chatBody.getTo();
            if(!ChatKit.isOnline(message.getTenantId(), to, true)){
                String from = chatBody.getFrom();
                String key = RedisKeyUtils.PushCache.friendUserOfflineKey(false, message.getTenantId(), to, from);
                cacheMessageHelper.writeMessage(ImConst.PREFIX_PUSH, key, chatBody, imChannelContext);
            }
        }
    }

    /**
     * 保存群组消息/组推送离线消息
     *
     * @param groupId 群组id
     * @param message 消息体
     */
    private void pushGroupMessages(String groupId, Message message, ImChannelContext imChannelContext){
        ImServerConfig imServerConfig = ImConfig.Global.get();
        RedisCache groupCache = RedisCacheManager.getCache(ImConst.PREFIX_GROUP);
        Group group = groupCache.get(RedisKeyUtils.GroupCache.groupInfoKey(false, message.getTenantId(), groupId), Group.class);
        if (Objects.isNull(group)) {
            return;
        }
        Map<Object, Object> userMap =  groupCache.hGetAll(RedisKeyUtils.GroupCache.groupUserInfoKey(false, imChannelContext.getSessionContext().getTempLinkId(), message.getTenantId(), groupId));
        List<User> groupUsers = userMap.entrySet().stream().map(e -> JSON.parseObject(e.getValue().toString(), User.class)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(groupUsers)) {
            // 缓存中没有组数据，则从数据库查询
            groupUsers = userService.selectGroupUserByGroupId(groupId);
        }

        //通过写扩散模式将群消息同步到所有的群成员
        for(User user : groupUsers){
            boolean isOnline = false;
            if(ImServerConfig.ON.equals(imServerConfig.getIsCluster())){
                isOnline = cacheMessageHelper.isOnline(message.getTenantId(), user.getUserId());
            }else{
                isOnline = ChatKit.isOnline(message.getTenantId(), user.getUserId(), true);
            }
            if(!isOnline){
                // 保存群组聊天离线消息
                if (Command.COMMAND_CHAT_REQ.getNumber() == message.getCmd().intValue()) {
                    String key = RedisKeyUtils.PushCache.groupUserOfflineKey(false, message.getTenantId(), groupId, user.getUserId());
                    cacheMessageHelper.writeMessage(ImConst.PREFIX_PUSH, key, (ChatBody)message, imChannelContext);
                }
                // 保存通知离线消息
                if (Command.COMMAND_NOTIFY_REQ.getNumber() == message.getCmd().intValue()) {
                    saveCacheNotify(message.getTenantId(), message.getCreateTime(), user.getUserId(), message);
                }
            }
        }
    }

    /**
     * 保存聊天离线消息到缓存
     *
     * @param message
     */
    private void saveNotifyOffline(Message message, ImChannelContext imChannelContext) {
        NotifyReqBody notifyBody = (NotifyReqBody)message;
        // 保存广播离线消息
        if (NotifyTypeEnum.NOTIFY_TYPE_BROADCAST.getNumber() == notifyBody.getNotifyType()) {
            notifyTypeBroadcast(notifyBody);
        }

        // 保存组推送离线消息
        if (NotifyTypeEnum.NOTIFY_TYPE_GROUP.getNumber() == notifyBody.getNotifyType()) {
            for (String toGroupId : notifyBody.getToGroupIds()) {
                pushGroupMessages(toGroupId, message, imChannelContext);
            }
        }

        // 保存精确推送离线消息
        if (NotifyTypeEnum.NOTIFY_TYPE_ACCURATE.getNumber() == notifyBody.getNotifyType()) {
            for (String toUserId : notifyBody.getToUserIds()) {
                if(!NotifyKit.isOnline(message.getTenantId(), toUserId, true)){
                    saveCacheNotify(notifyBody.getTenantId(), notifyBody.getCreateTime(), toUserId, notifyBody);
                }
            }
        }
    }

    /**
     * 广播-保存离线消息
     * @param notifyBody
     */
    private void notifyTypeBroadcast(NotifyReqBody notifyBody) {
        // 得到所有用户
        for (String tenantId : notifyBody.getTenantIds()) {
            List<UserDto> userDtos = userService.selectUsers(tenantId);
            if (CollectionUtils.isEmpty(userDtos)) {
                continue;
            }
            // 遍历判断用户是否在线, 保存离线通知
            for (UserDto userDto : userDtos) {
                String uid = userDto.getId();
                if(!NotifyKit.isOnline(notifyBody.getTenantId(), uid , true)){
                    saveCacheNotify(notifyBody.getTenantId(), notifyBody.getCreateTime(), uid, notifyBody);
                }
            }
        }
    }

    /**
     * 保存通知离线消息
     *
     * @param tenantId 租户id
     * @param createTime 消息时间戳
     * @param toUserId 目标用户id
     * @param content 缓存内容
     */
    private void saveCacheNotify(String tenantId, Long createTime, String toUserId, Serializable content) {
        String key = RedisKeyUtils.PushCache.notifyUserOfflineKey(false, tenantId, toUserId);
        RedisCacheManager.getCache(ImConst.PREFIX_PUSH).sortSetPush(key, createTime.doubleValue(), content);
    }

    /**
     * 业务自定义消息保存
     * @param message
     */
    private void saveBusinessMessage(Message message) {
        BusinessBody businessBody = (BusinessBody) message;
        // 不保存null，但可保存{}
        if (businessBody.getContent() == null) {
            return;
        }
        String tenantId = businessBody.getTenantId();
        String businessId = businessBody.getBusinessId();
        String key = RedisKeyUtils.BusinessCache.businessKey(false, tenantId, businessId);
        RedisCacheManager.getCache(ImConst.PREFIX_BUSINESS).put(key, businessBody, businessBody.getExpire());
    }


    @Override
    public boolean isOnline(String tenantId, String userId) {
         return cacheMessageHelper.isOnline(tenantId, userId);
    }

    @Override
    public UserMessageData getGroupOfflineMessage(String tenantId, String userId, String groupId) {
        return cacheMessageHelper.getGroupOfflineMessage(tenantId, userId, groupId);
    }

    @Override
    public UserMessageData getFriendsOfflineMessage(String tenantId, String userId) {
        return cacheMessageHelper.getFriendsOfflineMessage(tenantId, userId);
    }

    @Override
    public UserMessageData getFriendsOfflineMessage(String tenantId, String userId, String fromUserId) {
        return cacheMessageHelper.getFriendsOfflineMessage(tenantId, userId, fromUserId);
    }

    @Override
    public UserMessageData getNotifyOfflineMessage(String tenantId, String userId) {
        return cacheMessageHelper.getNotifyOfflineMessage(tenantId, userId);
    }

    @Override
    public List<String> getGroups(String tenantId, String userId) {
        return cacheMessageHelper.getGroups(tenantId, userId);
    }

    @Override
    public User getUserByType(String tenantId, String userId, Integer type) {
        return cacheMessageHelper.getUserByType(tenantId, userId, type);
    }

    @Override
    public List<Group> getAllFriendUsers(String tenantId, String userId, Integer type) {
        List<User> friends = socialService.getFriends(tenantId, userId);
        if (CollectionUtils.isEmpty(friends)) {
            return Collections.emptyList();
        }
        List<Group> friendList = new ArrayList<>();
        Group myFriend = Group.newBuilder().name("我的好友").build();
        List<User> myFriends = new ArrayList<>();

        List<User> onlineFriends = new ArrayList<>();
        List<User> offlineFriends = new ArrayList<>();

        // 判断在线状态
        for (User friend : friends) {
            boolean isOnline = this.isOnline(tenantId, friend.getUserId());
            String status = isOnline ? UserStatusType.ONLINE.getStatus() : UserStatusType.OFFLINE.getStatus();
            friend.setStatus(status);

            if(UserStatusType.ONLINE.getNumber() == type && UserStatusType.ONLINE.getStatus().equals(status)){
                onlineFriends.add(friend);
            }else if(UserStatusType.OFFLINE.getNumber() == type && UserStatusType.OFFLINE.getStatus().equals(status)){
                offlineFriends.add(friend);
            }else if(UserStatusType.ALL.getNumber() == type){
                if (UserStatusType.ONLINE.getStatus().equals(status)) {
                    onlineFriends.add(friend);
                } else {
                    offlineFriends.add(friend);
                }
            }
        }

        myFriends.addAll(onlineFriends);
        myFriends.addAll(offlineFriends);
        myFriend.setOnline(onlineFriends.size());
        myFriend.setUsers(myFriends);
        friendList.add(myFriend);
        return friendList;
    }

    @Override
    public Group getGroupUsers(String tenantId, String groupId, Integer type, ImChannelContext imChannelContext) {
        return cacheMessageHelper.getGroupUsers(tenantId, groupId, type, imChannelContext);
    }

    @Override
    public List<Group> getAllGroupUsers(String tenantId, String userId, Integer type, ImChannelContext imChannelContext) {
        return cacheMessageHelper.getAllGroupUsers(tenantId, userId, type, imChannelContext);
    }

    @Override
    public UserMessageData getBusinessMessage(String tenantId, String businessId) {
        UserMessageData messageData = new UserMessageData();
        String key = RedisKeyUtils.BusinessCache.businessKey(false, tenantId, businessId);
        String message = RedisCacheManager.getCache(ImConst.PREFIX_BUSINESS).get(key, String.class);
        if(StringUtils.isBlank(message)) {
            return messageData;
        }
        BusinessBody businessBody = JsonKit.toBean(message, BusinessBody.class);
        messageData.setBusiness(businessBody);
        return messageData;
    }

    @Override
    public void updateGroupUser(String userId, GroupUserUpdateReqBody reqBody, ImChannelContext imChannelContext) {
        RedisCache groupCache = RedisCacheManager.getCache(ImConst.PREFIX_GROUP);
        String groupUserInfoKey = RedisKeyUtils.GroupCache.groupUserInfoKey(false, imChannelContext.getSessionContext().getTempLinkId(), reqBody.getTenantId(), reqBody.getGroupId());
        User user = groupCache.hGet(groupUserInfoKey, userId, User.class);
        if (user == null) {
            return;
        }
        JSONObject extras = Optional.ofNullable(user.getExtras()).orElse(new JSONObject());
        extras.putAll(reqBody.getExtras());
        user.setExtras(extras);
        groupCache.hPut(groupUserInfoKey, userId, user);
    }
}
