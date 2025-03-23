package org.jim.core.message;

import org.jim.core.ImChannelContext;
import org.jim.core.enums.ChannelDataBusinessTypeEnum;
import org.jim.core.enums.ChannelDataStoreTypeEnum;
import org.jim.core.listener.ImStoreBindListener;
import org.jim.core.packets.*;
import org.jim.core.packets.group.GroupUserUpdateReqBody;

import java.util.List;

/**
 * @author WChao
*/
public interface MessageHelper {
	/**
	 * 获取IM开启持久化时绑定/解绑群组、用户监听器;
	 * @return
	 */
	 ImStoreBindListener getBindListener();

	/**
	 * 判断用户是否在线
	 * @param tenantId 租户id
	 * @param userId 用户ID
	 * @return
	 */
	 boolean isOnline(String tenantId, String userId);

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
	 * 获取好友分组所有成员信息
	 * @param tenantId 租户id
	 * @param userId 用户ID
	 * @param friendGroupId 好友分组ID
	 * @param type(0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线])
	 * @return
	 */
	 Group getFriendUsers(String tenantId, String userId, String friendGroupId, Integer type);
	/**
	 * 获取好友分组所有成员信息
	 * @param tenantId 租户id
	 * @param userId 用户ID
	 * @param type(0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线])
	 * @return
	 */
	 List<Group> getAllFriendUsers(String tenantId, String userId, Integer type);
	/**
	 * 根据在线类型获取用户信息;
	 * @param tenantId 租户id
	 * @param userId 用户ID
	 * @param type(0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线])
	 * @return
	 */
	 User getUserByType(String tenantId, String userId, Integer type);
	/**
	 * 添加群组成员
	 * @param tenantId 租户id
	 * @param userId 用户ID
	 * @param groupId 群组ID
	 */
	 void addGroupUser(String tenantId, String userId, String groupId);
	/**
	 * 获取群组所有成员;
	 * @param tenantId 租户id
	 * @param groupId 群组ID
	 * @return
	 */
	 List<String> getGroupUsers(String tenantId, String groupId, ImChannelContext imChannelContext);
	/**
	 * 获取用户拥有的群组ID;
	 * @param tenantId 租户id
	 * @param userId 用户ID
	 * @return
	 */
	 List<String> getGroups(String tenantId, String userId);
	/**
	 * 消息持久化写入
	 * @param timelineTable 持久化表
	 * @param timelineId 持久化ID
	 * @param chatBody 消息记录
	 */
	 void writeMessage(String timelineTable, String timelineId , ChatBody chatBody, ImChannelContext imChannelContext);

	/**
	 * 消息持久化写入
	 * @param message 消息记录
	 */
	void writeMessage(Message message, ImChannelContext imChannelContext);

	/**
	 * 移除群组用户
	 * @param tenantId 租户id
	 * @param userId 用户ID
	 * @param groupId 群组ID
	 */
	 void removeGroupUser(String tenantId, String userId, String groupId);
	/**
	 * 获取与指定用户离线消息;
	 * @param tenantId 租户id
	 * @param userId 用户ID
	 * @param fromUserId 目标用户ID
	 * @return
	 */
	 UserMessageData getFriendsOfflineMessage(String tenantId, String userId, String fromUserId);
	/**
	 * 获取与所有用户离线消息;
	 * @param tenantId 租户id
	 * @param userId 用户ID
	 * @return
	 */
	 UserMessageData getFriendsOfflineMessage(String tenantId, String userId);

	/**
	 * 获取用户所有离线通知消息;
	 * @param tenantId 租户id
	 * @param userId 用户ID
	 * @return
	 */
	UserMessageData getNotifyOfflineMessage(String tenantId, String userId);

	/**
	 * 获取用户指定群组离线消息;
	 * @param tenantId 租户id
	 * @param userId 用户ID
	 * @param groupId 群组ID
	 * @return
	 */
	 UserMessageData getGroupOfflineMessage(String tenantId, String userId,String groupId);

	/**
	 * 获取与指定用户历史消息;
	 * @param tenantId 租户id
	 * @param userId 用户ID
	 * @param fromUserId 目标用户ID
	 * @param beginTime 消息区间开始时间
	 * @param endTime 消息区间结束时间
	 * @param offset 分页偏移量
	 * @param count 数量
	 * @return
	 */
	 UserMessageData getFriendHistoryMessage(String tenantId, String userId, String fromUserId, Long beginTime, Long endTime, Long offset, Long count);
	
	/**
	 * 获取与指定群组历史消息;
	 * @param tenantId 租户id
	 * @param userId 用户ID
	 * @param groupId 群组ID
	 * @param beginTime 消息区间开始时间
	 * @param endTime 消息区间结束时间
	 * @param offset 分页偏移量
	 * @param count 数量
	 * @return
	 */
	 UserMessageData getGroupHistoryMessage(String tenantId, String userId, String groupId, Long beginTime, Long endTime, Long offset, Long count);

	/**
	 * 获取所有通知历史消息;
	 * @param tenantId 租户id
	 * @param userId 用户ID
	 * @param beginTime 消息区间开始时间
	 * @param endTime 消息区间结束时间
	 * @param offset 分页偏移量
	 * @param count 数量
	 * @return
	 */
	UserMessageData getNotifyHistoryMessage(String tenantId, String userId, Long beginTime, Long endTime, Long offset, Long count);

	/**
	 * 更新用户终端协议类型及在线状态;(在线:online:离线:offline)
	 * @param user 用户信息
	 */
	 boolean updateUserTerminal(User user);

	/**
	 * 获取业务自定义消息
	 * @param tenantId 租户id
	 * @param businessId 业务id
	 * @return
	 */
	UserMessageData getBusinessMessage(String tenantId, String businessId);

	/**
	 * 修改组用户信息
	 * @param userId 用户id
	 * @param reqBody
	 */
	void updateGroupUser(String userId, GroupUserUpdateReqBody reqBody, ImChannelContext imChannelContext);

	/**
	 * 保存服务关闭后，需要处理的数据
	 * @param userId 用户id
	 * @param storeTypeEnum 存储方式
	 * @param businessTypeEnum 业务类型
	 * @param storeCode 存储唯一编号
	 * @param data 消息
	 */
	void saveChannelData(String userId, ChannelDataStoreTypeEnum storeTypeEnum, ChannelDataBusinessTypeEnum businessTypeEnum, String storeCode, Object data);

	/**
	 * 删除服务连接相关的数据
	 * @param userId 用户id
	 * @param businessTypeEnum 业务类型
	 * @param storeCode 存储唯一编号
	 */
	void deleteChannelData(String userId, ChannelDataBusinessTypeEnum businessTypeEnum, String storeCode);
}
