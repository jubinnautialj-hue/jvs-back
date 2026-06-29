package cn.bctools.im.dto.sync;

import lombok.Data;

/**
 * @author ZhuXiaoKang
 * @Description
 */
@Data
public class SyncChatMessageDto extends BaseSyncMessageDto{

    /**
     * 发送消息用户id
     */
    private String fromUserId;

    /**
     * 接收消息用户id
     */
    private String toUserId;

    /**
     * 聊天消息体
     */
    private String message;

    /**
     * 组id
     */
    private String groupId;

}
