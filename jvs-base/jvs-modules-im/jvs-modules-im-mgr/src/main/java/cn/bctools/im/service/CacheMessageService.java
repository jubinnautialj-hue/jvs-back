package cn.bctools.im.service;

import org.jim.core.ImChannelContext;
import org.jim.core.packets.Group;
import org.jim.core.packets.Message;
import org.jim.core.packets.User;
import org.jim.core.packets.UserMessageData;
import org.jim.core.packets.group.GroupUserUpdateReqBody;

import java.util.List;

/**
 * @author ZhuXiaoKang
 * @Description 缓存处理
 */
public interface CacheMessageService {

    /**
     * 缓存消息入库
     *
     * @param message 消息体
     */
    void saveMessage(Message message, ImChannelContext imChannelContext);


    /**
     * 判断用户是否在线
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @return true：在线， false：离线
     */
    boolean isOnline(String tenantId, String userId);

    /**
     * 获取用户指定群组离线消息
     *
     * @param tenantId 租户id
     * @param userId 用户ID
     * @param groupId 群组ID
     * @return
     */
    UserMessageData getGroupOfflineMessage(String tenantId, String userId, String groupId);

    /**
     * 获取与所有用户离线消息
     *
     * @param tenantId 租户id
     * @param userId 用户ID
     * @return
     */
    UserMessageData getFriendsOfflineMessage(String tenantId, String userId);

    /**
     * 获取与指定用户离线消息
     *
     * @param tenantId 租户id
     * @param userId 用户ID
     * @param fromUserId 目标用户ID
     * @return
     */
    UserMessageData getFriendsOfflineMessage(String tenantId, String userId, String fromUserId);

    /**
     * 获取用户所有离线通知消息;
     *
     * @param tenantId 租户id
     * @param userId 用户ID
     * @return
     */
    UserMessageData getNotifyOfflineMessage(String tenantId, String userId);

    /**
     * 获取用户拥有的所有群组
     * @param tenantId
     * @param userId
     * @return
     */
    List<String> getGroups(String tenantId, String userId);

    /**
     * 根据在线类型获取用户信息;
     * @param tenantId 租户id
     * @param userId 用户ID
     * @param type(0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线])
     * @return
     */
    User getUserByType(String tenantId, String userId, Integer type);

    /**
     * 获取好友分组所有成员信息
     * @param userId 用户ID
     * @param type(0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线])
     * @return
     */
    List<Group> getAllFriendUsers(String tenantId, String userId, Integer type);

    /**
     * 获取指定群组所有成员信息
     * @param tenantId 租户id
     * @param groupId 群组ID
     * @param type(0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线])
     * @return
     */
    Group getGroupUsers(String tenantId, String groupId, Integer type, ImChannelContext imChannelContext);

    /**
     * 获取用户所有群组成员信息
     * @param tenantId 租户id
     * @param userId 用户ID
     * @param type(0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线])
     * @return
     */
    List<Group> getAllGroupUsers(String tenantId, String userId, Integer type, ImChannelContext imChannelContext);

    /**
     * 获取业务消息
     * @param tenantId
     * @param businessId
     * @return
     */
    UserMessageData getBusinessMessage(String tenantId, String businessId);

    /**
     * 修改组用户信息
     * @param userId 用户id
     * @param reqBody 用户信息
     */
    void updateGroupUser(String userId, GroupUserUpdateReqBody reqBody, ImChannelContext imChannelContext);
}
