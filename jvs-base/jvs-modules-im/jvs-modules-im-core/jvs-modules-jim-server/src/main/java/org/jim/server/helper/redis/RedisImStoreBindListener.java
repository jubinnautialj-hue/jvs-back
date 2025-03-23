package org.jim.server.helper.redis;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jim.core.ImChannelContext;
import org.jim.core.ImSessionContext;
import org.jim.core.cache.redis.RedisCache;
import org.jim.core.cache.redis.RedisCacheManager;
import org.jim.core.config.ImConfig;
import org.jim.core.enums.ChannelDataBusinessTypeEnum;
import org.jim.core.enums.ChannelDataStoreTypeEnum;
import org.jim.core.exception.ImException;
import org.jim.core.listener.AbstractImStoreBindListener;
import org.jim.core.message.MessageHelper;
import org.jim.core.packets.Group;
import org.jim.core.packets.GroupTypeEnum;
import org.jim.core.packets.User;
import org.jim.core.packets.UserStatusType;
import org.jim.server.config.ImServerConfig;
import org.jim.server.util.MarkUtils;
import org.jim.server.util.RedisKeyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 消息持久化绑定监听器
 *
 * @author WChao
*/
public class RedisImStoreBindListener extends AbstractImStoreBindListener {

    private static Logger logger = LoggerFactory.getLogger(RedisImStoreBindListener.class);

    private static final String SUFFIX = ":";

    public RedisImStoreBindListener(ImConfig imConfig, MessageHelper messageHelper, MessageHelper dbHelper) {
        super(imConfig, messageHelper, dbHelper);
    }

    @Override
    public void onAfterGroupBind(ImChannelContext imChannelContext, Group group) throws ImException {
        if (!isStore()) {
            return;
        }
        initGroupUsers(group, imChannelContext);
    }

    @Override
    public void onAfterGroupUnbind(ImChannelContext imChannelContext, Group group) throws ImException {
        if (!isStore()) {
            return;
        }
        String tenantId = imChannelContext.getTenantId();
        String userId = MarkUtils.parseMark(imChannelContext.getUserId());
        String tempLinkId = imChannelContext.getSessionContext().getTempLinkId();
        String groupId = MarkUtils.parseMark(group.getGroupId());

        if (StringUtils.isBlank(userId)) {
           return;
        }
        if (GroupTypeEnum.BUSINESS.getNumber() == group.getGroupType()) {
            // 移除群组成员;
            RedisCache groupCache = RedisCacheManager.getCache(PREFIX_GROUP);
            groupCache.listRemove(RedisKeyUtils.GroupCache.groupUserKey(false, tempLinkId, tenantId, groupId), userId);
            dbHelper.deleteChannelData(userId, ChannelDataBusinessTypeEnum.GROUP_USER_IDS, RedisKeyUtils.GroupCache.groupUserKey(true, tempLinkId, tenantId, groupId));
            groupCache.hDelete(RedisKeyUtils.GroupCache.groupUserInfoKey(false, tempLinkId, tenantId, groupId), userId);
            dbHelper.deleteChannelData(userId, ChannelDataBusinessTypeEnum.GROUP_USER_INFO, RedisKeyUtils.GroupCache.groupUserInfoKey(true, tempLinkId, tenantId, groupId));
            // 若组成员为空，重新设置组信息有效时长
            String groupUserKey = RedisKeyUtils.GroupCache.groupUserKey(false, tempLinkId, tenantId, groupId);
            List<String> groupUserInfos = groupCache.listGetAll(groupUserKey);
            if (CollectionUtils.isEmpty(groupUserInfos)) {
                groupCache.expire(RedisKeyUtils.GroupCache.groupInfoKey(false, tenantId, groupId), 60, TimeUnit.SECONDS);
            }
            // 移除成员群组;
            RedisCacheManager.getCache(PREFIX_USER).listRemove(RedisKeyUtils.UserCache.userGroupKey(false, tenantId, userId), groupId);
            // 移除群组离线消息
            RedisCacheManager.getCache(PREFIX_PUSH).remove(RedisKeyUtils.PushCache.groupUserOfflineKey(false, tenantId, groupId, userId));
        }
    }


    @Override
    public void onAfterUserBind(ImChannelContext imChannelContext, User user) throws ImException {
        if (!isStore() || Objects.isNull(user)) {
            return;
        }
        user.setStatus(UserStatusType.ONLINE.getStatus());
        this.messageHelper.updateUserTerminal(user);
//		initUserInfo(user);
    }

    @Override
    public void onAfterUserUnbind(ImChannelContext imChannelContext, User user) throws ImException {
        if (!isStore() || Objects.isNull(user)) {
            return;
        }
        user.setStatus(UserStatusType.OFFLINE.getStatus());
        this.messageHelper.updateUserTerminal(user);
    }

    /**
     * 初始化群组用户;
     *
     * @param group
     * @param imChannelContext
     */
    public void initGroupUsers(Group group, ImChannelContext imChannelContext) {
        String groupId = MarkUtils.parseMark(group.getGroupId());
        String tempLinkId = imChannelContext.getSessionContext().getTempLinkId();
        // 不持久化
        if (!isStore()) {
            return;
        }
        String userId = MarkUtils.parseMark(imChannelContext.getUserId());
        if (StringUtils.isEmpty(groupId) || StringUtils.isEmpty(userId)) {
            return;
        }
        String tenantId = imChannelContext.getTenantId();
        String groupUserKey = RedisKeyUtils.GroupCache.groupUserKey(false, tempLinkId, tenantId, groupId);
        RedisCache groupCache = RedisCacheManager.getCache(PREFIX_GROUP);
        List<String> users = groupCache.listGetAll(groupUserKey);
        if (!users.contains(userId)) {
            groupCache.listPushTail(groupUserKey, userId);
        }

        initUserGroups(tenantId, userId, groupId);
        ImSessionContext imSessionContext = imChannelContext.getSessionContext();
        User onlineUser = imSessionContext.getImClientNode().getUser();
        if (onlineUser == null) {
            return;
        }

        List<Group> groups = onlineUser.getGroups();
        if (CollectionUtils.isNotEmpty(groups)) {
            for (Group storeGroup : groups) {
                // 未在缓存中的组成员，加入缓存
                Map<Object, Object> cacheUserMap =  RedisCacheManager.getCache(PREFIX_GROUP).hGetAll(RedisKeyUtils.GroupCache.groupUserInfoKey(false, tempLinkId, tenantId, groupId));
                List<User> userList = Optional.ofNullable(storeGroup.getUsers()).orElse(new ArrayList<>());
                Map<String, Object> userMap = userList.stream().filter(u -> !cacheUserMap.containsKey(u.getUserId())).collect(Collectors.toMap(User::getUserId, v -> {v.setGroups(null); v.setFriends(null); return v;}));
                // 组信息缓存不保存成员信息
                storeGroup.setUsers(null);
                groupCache.put(RedisKeyUtils.GroupCache.groupInfoKey(false, tenantId, storeGroup.getGroupId()), storeGroup);
                // 缓存组成员信息
                groupCache.hPutAll(RedisKeyUtils.GroupCache.groupUserInfoKey(false, tempLinkId, tenantId, storeGroup.getGroupId()), userMap);
            }
        }

        // 修改组当前用户信息
        onlineUser.setFriends(null);
        onlineUser.setGroups(null);
        groupCache.hPut(RedisKeyUtils.GroupCache.groupUserInfoKey(false, tempLinkId, tenantId, groupId), onlineUser.getUserId(), onlineUser);
        if (GroupTypeEnum.BUSINESS.getNumber() == group.getGroupType()) {
            dbHelper.saveChannelData(userId, ChannelDataStoreTypeEnum.REDIS, ChannelDataBusinessTypeEnum.GROUP_USER_IDS, RedisKeyUtils.GroupCache.groupUserKey(true, tempLinkId, tenantId, groupId) , null);
            dbHelper.saveChannelData(userId, ChannelDataStoreTypeEnum.REDIS, ChannelDataBusinessTypeEnum.GROUP_USER_INFO, RedisKeyUtils.GroupCache.groupUserInfoKey(true, tempLinkId, tenantId, groupId) , null);
        }
    }

    /**
     * 初始化用户拥有哪些群组;
     *
     * @param tenantId
     * @param userId
     * @param group
     */
    public void initUserGroups(String tenantId, String userId, String group) {
        if (!isStore()) {
            return;
        }
        if (StringUtils.isEmpty(group) || StringUtils.isEmpty(userId)) {
            return;
        }
        List<String> groups = RedisCacheManager.getCache(PREFIX_USER).listGetAll(RedisKeyUtils.UserCache.userGroupKey(false, tenantId, userId));
        if (groups.contains(group)) {
            return;
        }
        RedisCacheManager.getCache(PREFIX_USER).listPushTail(RedisKeyUtils.UserCache.userGroupKey(false, tenantId, userId), group);
    }

    /**
     * 初始化用户终端协议类型;
     *
     * @param user
     */
    public void initUserInfo(User user) {
        if (!isStore() || user == null) {
            return;
        }
        String userId = user.getUserId();
        if (StringUtils.isEmpty(userId)) {
            return;
        }
        RedisCache userCache = RedisCacheManager.getCache(PREFIX_USER);
        userCache.put(RedisKeyUtils.UserCache.userInfoKey(false,user.getTenantId(), userId), user.clone());
        List<Group> friends = user.getFriends();
        if (CollectionUtils.isEmpty(friends)) {
            return;
        }
        userCache.put(RedisKeyUtils.UserCache.userFriendKey(false, user.getTenantId(), userId), (Serializable) friends);
    }

    /**
     * 是否开启持久化;
     *
     * @return
     */
    public boolean isStore() {
        ImServerConfig imServerConfig = ImServerConfig.Global.get();
        return ImServerConfig.ON.equals(imServerConfig.getIsStore());
    }

    static {
        RedisCacheManager.register(PREFIX_USER, Integer.MAX_VALUE, Integer.MAX_VALUE);
        RedisCacheManager.register(PREFIX_GROUP, Integer.MAX_VALUE, Integer.MAX_VALUE);
        RedisCacheManager.register(PREFIX_STORE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        RedisCacheManager.register(PREFIX_PUSH, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

}
