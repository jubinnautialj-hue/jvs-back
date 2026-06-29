package cn.bctools.im.helper;
import cn.bctools.common.utils.SpringContextUtil;
import com.google.common.collect.Maps;

import cn.bctools.im.dto.history.HistoryMessageReqDto;
import cn.bctools.im.entity.ChannelData;
import cn.bctools.im.entity.enums.MessageTypeEnum;
import cn.bctools.im.message.RabbitMqTemplate;
import cn.bctools.im.message.constant.RabbitMqConstant;
import cn.bctools.im.message.dto.BaseSyncMessageDto;
import cn.bctools.im.message.dto.SyncChatMessageDto;
import cn.bctools.im.message.dto.SyncNotifyMessageDto;
import cn.bctools.im.service.CacheMessageService;
import cn.bctools.im.service.ChannelDataService;
import cn.bctools.im.service.ChannelDataStrategy;
import cn.bctools.im.service.ImMessageService;
import cn.bctools.im.strategy.SyncMessageStrategy;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jim.core.ImChannelContext;
import org.jim.core.cluster.ImClusterVO;
import org.jim.core.enums.ChannelDataBusinessTypeEnum;
import org.jim.core.enums.ChannelDataStoreTypeEnum;
import org.jim.core.listener.ImStoreBindListener;
import org.jim.core.message.AbstractMessageHelper;
import org.jim.core.packets.*;
import org.jim.core.packets.group.GroupUserUpdateReqBody;
import org.jim.core.packets.notify.NotifyReqBody;
import org.jim.core.utils.JsonKit;
import org.jim.server.helper.redis.RedisImStoreBindListener;
import org.jim.server.helper.redis.RedisMessageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author ZhuXiaoKang
 * @Description 处理IM持久化帮助类
 */
@Slf4j
@Component
public class DbMessageHelper extends AbstractMessageHelper {


    @Autowired
    private RabbitMqTemplate rabbitMqTemplate;

    @Autowired
    private CacheMessageService cacheMessageService;

    @Autowired
    private SyncMessageStrategy syncMessageStrategy;

    @Autowired
    private ChannelDataStrategy channelDataStrategy;

    @Override
    public ImStoreBindListener getBindListener() {
        DbMessageHelper dbMessageHelper = SpringContextUtil.getBean(DbMessageHelper.class);
        return new RedisImStoreBindListener(imConfig, new RedisMessageHelper(), dbMessageHelper);
    }


    @Override
    public boolean isOnline(String tenantId, String userId) {
        return cacheMessageService.isOnline(tenantId, userId);
    }

    @Override
    public Group getGroupUsers(String tenantId, String groupId, Integer type, ImChannelContext imChannelContext) {
        return cacheMessageService.getGroupUsers(tenantId, groupId, type, imChannelContext);
    }

    @Override
    public List<Group> getAllGroupUsers(String tenantId, String userId, Integer type, ImChannelContext imChannelContext) {
        return cacheMessageService.getAllGroupUsers(tenantId, userId, type, imChannelContext);
    }

    @Override
    public Group getFriendUsers(String tenantId, String userId, String friendGroupId, Integer type) {
        return null;
    }

    @Override
    public List<Group> getAllFriendUsers(String tenantId, String userId, Integer type) {
        return cacheMessageService.getAllFriendUsers(tenantId, userId, type);
    }

    @Override
    public User getUserByType(String tenantId, String userId, Integer type) {
        return cacheMessageService.getUserByType(tenantId, userId, type);
    }

    @Override
    public void addGroupUser(String tenantId, String userId, String groupId) {
        // 暂无实现
    }

    @Override
    public List<String> getGroupUsers(String tenantId, String groupId, ImChannelContext imChannelContext) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getGroups(String tenantId, String userId) {
        return cacheMessageService.getGroups(tenantId, userId);
    }

    @Override
    public void writeMessage(String timelineTable, String timelineId, ChatBody chatBody, ImChannelContext imChannelContext) {
        // 暂无实现
    }

    @Override
    public void writeMessage(Message message, ImChannelContext imChannelContext) {
        if (message == null) {
            return;
        }
        BaseSyncMessageDto syncMessageDto = null;
        // 聊天消息转换
        if (Command.COMMAND_CHAT_REQ.getNumber() == message.getCmd().intValue()) {
            ChatBody chatBody = (ChatBody)message;
            syncMessageDto = convertChat(chatBody);
        }
        // 通知消息转换
        if (Command.COMMAND_NOTIFY_REQ.getNumber() == message.getCmd().intValue()) {
            NotifyReqBody notifyReqBody = (NotifyReqBody)message;
            syncMessageDto = convertNotify(notifyReqBody);
        }
        // 将消息发送到RabbitMQ，由消费者将消息保存到数据库
        if (syncMessageDto != null) {
            rabbitMqTemplate.send(syncMessageDto);
        }

        // 缓存消息入库(redis)
        cacheMessageService.saveMessage(message, imChannelContext);
    }


    /**
     * 聊天消息转换
     * @param chatBody
     * @return
     */
    private BaseSyncMessageDto convertChat(ChatBody chatBody) {
        try {
            SyncChatMessageDto syncChatMessageDto = new SyncChatMessageDto();
            syncChatMessageDto.setTopic(RabbitMqConstant.ROUTING_SYNC_CHAT);
            syncChatMessageDto.setChatType(chatBody.getChatType());
            syncChatMessageDto.setFromUserId(chatBody.getFrom());
            syncChatMessageDto.setCreateTime(chatBody.getCreateTime());
            syncChatMessageDto.setMessage(JSON.toJSONString(chatBody));
            syncChatMessageDto.setTenantId(chatBody.getTenantId());
            // 存储群聊消息
            if(ChatType.CHAT_TYPE_PUBLIC.getNumber() == chatBody.getChatType()){
                syncChatMessageDto.setGroupId(chatBody.getGroupId());
                return syncChatMessageDto;
            }
            // 存储私聊消息
            if(ChatType.CHAT_TYPE_PRIVATE.getNumber() == chatBody.getChatType()){
                syncChatMessageDto.setToUserId(chatBody.getTo());
                return syncChatMessageDto;
            }
        } catch (Exception e) {
            log.warn("聊天消息转换失败: {}", JSON.toJSONString(chatBody));
        }
        return null;
    }

    /**
     * 通知消息转换
     * @param notifyReqBody
     * @return
     */
    private BaseSyncMessageDto convertNotify(NotifyReqBody notifyReqBody) {
        try {
            SyncNotifyMessageDto syncNotifyMessageDto = new SyncNotifyMessageDto();
            syncNotifyMessageDto.setTopic(RabbitMqConstant.ROUTING_SYNC_NOTIFY);
            syncNotifyMessageDto.setCreateTime(notifyReqBody.getCreateTime());
            syncNotifyMessageDto.setFromUserId(notifyReqBody.getFrom());
            syncNotifyMessageDto.setGroupIds(JSON.toJSONString(notifyReqBody.getToGroupIds()));
            syncNotifyMessageDto.setUserIds(JSON.toJSONString(notifyReqBody.getToUserIds()));
            syncNotifyMessageDto.setTenantIds(JSON.toJSONString(notifyReqBody.getTenantIds()));
            syncNotifyMessageDto.setMessage(JSON.toJSONString(notifyReqBody));
            syncNotifyMessageDto.setNotifyType(notifyReqBody.getNotifyType());
            syncNotifyMessageDto.setTenantId(notifyReqBody.getTenantId());
            return syncNotifyMessageDto;
        } catch (Exception e) {
            log.warn("通知消息转换失败: {}", JSON.toJSONString(notifyReqBody));
        }
        return null;
    }

    @Override
    public void removeGroupUser(String tenantId, String userId, String groupId) {
        // 暂无实现
    }

    @Override
    public UserMessageData getFriendsOfflineMessage(String tenantId, String userId, String fromUserId) {
        return cacheMessageService.getFriendsOfflineMessage(tenantId, userId, fromUserId);
    }

    @Override
    public UserMessageData getFriendsOfflineMessage(String tenantId, String userId) {
        return cacheMessageService.getFriendsOfflineMessage(tenantId, userId);
    }

    @Override
    public UserMessageData getNotifyOfflineMessage(String tenantId, String userId) {
        return cacheMessageService.getNotifyOfflineMessage(tenantId, userId);
    }

    @Override
    public UserMessageData getGroupOfflineMessage(String tenantId, String userId, String groupId) {
        return cacheMessageService.getGroupOfflineMessage(tenantId, userId, groupId);
    }


    /**
     * 分页查询历史消息
     *
     * @param imMessageService 服务
     * @param tenantId 租户id
     * @param userId 登录用户id
     * @param fromUserId 发送消息用户id
     * @param userGroupIds 用户所属组id集合
     * @param groupId 目标组id
     * @param beginTime 查询开始时间戳
     * @param endTime 查询结束时间戳
     * @param offset 页码
     * @param count 每页数据量
     * @return
     */
    private Page<String> pageHistoryMessage(ImMessageService imMessageService, String tenantId, String userId, String fromUserId, List<String> userGroupIds, String groupId, Long beginTime, Long endTime, Long offset, Long count) {
        try {
            HistoryMessageReqDto reqDto = new HistoryMessageReqDto();
            reqDto.setTenantId(tenantId);
            reqDto.setUserId(userId);
            reqDto.setBeginTime(beginTime);
            reqDto.setEndTime(endTime);
            reqDto.setUserGroupIds(userGroupIds);
            reqDto.setGroupId(groupId);
            reqDto.setFromUserId(fromUserId != null ? fromUserId : null);
            reqDto.setCurrent(offset);
            reqDto.setSize(count);

            return imMessageService.pageMessageHistory(reqDto);
        } catch (Exception e) {
            log.error("查询群组历史消息失败, userGroupIds:[{}], userId:[{}], groupId:[{}], exception: [{}]", userGroupIds, userId, groupId, e);
        }
        return null;
    }

    @Override
    public UserMessageData getFriendHistoryMessage(String tenantId, String userId, String fromUserId, Long beginTime, Long endTime, Long offset, Long count) {
        UserMessageData messageData = new UserMessageData(userId);
        // 查询历史数据
        ImMessageService imMessageService = syncMessageStrategy.getImMessageService(MessageTypeEnum.CHAT_PRIVATE.getValue());
        Page<String> historyResPage = pageHistoryMessage(imMessageService, tenantId, userId, fromUserId, null, null, beginTime, endTime, offset, count);
        if (historyResPage == null) {
            return messageData;
        }
        // 封装响应
        List<ChatBody> messageList = new ArrayList<>();
        messageList.addAll(JsonKit.toArray(historyResPage.getRecords(), ChatBody.class));
        messageData.getFriends().put(userId, messageList);
        messageData.setTotal(historyResPage.getTotal());
        messageData.setSize(historyResPage.getSize());
        messageData.setCurrent(historyResPage.getCurrent());
        return messageData;
    }

    @Override
    public UserMessageData getGroupHistoryMessage(String tenantId, String userId, String groupId, Long beginTime, Long endTime, Long offset, Long count) {
        UserMessageData messageData = new UserMessageData(userId);
        // 查询历史数据
        ImMessageService imMessageService = syncMessageStrategy.getImMessageService(MessageTypeEnum.CHAT_GROUP.getValue());
        List<String> groupIds = getUserGroups(tenantId, userId);
        Page<String> historyResPage = pageHistoryMessage(imMessageService, tenantId, userId, null, groupIds, groupId, beginTime, endTime, offset, count);
        if (historyResPage == null) {
            return messageData;
        }
        // 封装响应
        List<ChatBody> messageList = new ArrayList<>();
        messageList.addAll(JsonKit.toArray(historyResPage.getRecords(), ChatBody.class));
        messageData.getGroups().put(groupId, messageList);
        messageData.setTotal(historyResPage.getTotal());
        messageData.setSize(historyResPage.getSize());
        messageData.setCurrent(historyResPage.getCurrent());
        return messageData;
    }

    @Override
    public boolean updateUserTerminal(User user) {
        return false;
    }

    @Override
    public UserMessageData getNotifyHistoryMessage(String tenantId, String userId, Long beginTime, Long endTime, Long offset, Long count) {
        UserMessageData messageData = new UserMessageData(userId);
        // 查询历史数据
        ImMessageService imMessageService = syncMessageStrategy.getImMessageService(MessageTypeEnum.NOTIFY.getValue());
        // 获取用户群组id
        List<String> groupIds = getUserGroups(tenantId, userId);
        // 得到数据
        Page<String> historyResPage = pageHistoryMessage(imMessageService, tenantId, userId, null,  groupIds, null, beginTime, endTime, offset, count);
        if (historyResPage == null) {
            return messageData;
        }
        // 封装响应
        messageData.setNotifys(JsonKit.toArray(historyResPage.getRecords(), NotifyReqBody.class));
        messageData.setTotal(historyResPage.getTotal());
        messageData.setSize(historyResPage.getSize());
        messageData.setCurrent(historyResPage.getCurrent());
        return messageData;
    }

    /**
     * 得到用户群组id集合
     * @param tenantId
     * @param userId
     * @return
     */
    private List<String> getUserGroups(String tenantId, String userId) {
        return getGroups(tenantId, userId);
    }

    @Override
    public UserMessageData getBusinessMessage(String tenantId, String businessId) {
        return cacheMessageService.getBusinessMessage(tenantId, businessId);
    }

    @Override
    public void updateGroupUser(String userId,  GroupUserUpdateReqBody reqBody, ImChannelContext imChannelContext) {
        cacheMessageService.updateGroupUser(userId, reqBody, imChannelContext);
    }

    @Override
    public void saveChannelData(String userId, ChannelDataStoreTypeEnum storeTypeEnum, ChannelDataBusinessTypeEnum businessTypeEnum, String storeCode, Object data) {
        ChannelDataService channelDataService = channelDataStrategy.getService(businessTypeEnum.getValue());
        ChannelData channelData = new ChannelData();
        channelData.setServerId(ImClusterVO.CLIENT_ID);
        channelData.setUserId(userId);
        channelData.setStoreType(storeTypeEnum.getValue());
        channelData.setStoreCode(storeCode);
        channelData.setBusinessType(businessTypeEnum.getValue());
        channelData.setProcessStatus(Boolean.FALSE);
        channelDataService.save(channelData, data);
    }

    @Override
    public void deleteChannelData(String userId, ChannelDataBusinessTypeEnum businessTypeEnum, String storeCode) {
        ChannelDataService channelDataService = channelDataStrategy.getService(businessTypeEnum.getValue());
        channelDataService.delete(ImClusterVO.CLIENT_ID, userId, storeCode);
    }
}
