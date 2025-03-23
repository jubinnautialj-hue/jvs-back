package cn.bctools.im.dto.sync;

import lombok.Data;

/**
 * @author ZhuXiaoKang
 * @Description
 */
@Data
public class SyncChatGroupMessageDto extends BaseSyncMessageDto {

    /**
     * 群组id
     */
    private String groupId;

    /**
     * 发送消息用户id
     */
    private String fromUserId;

    /**
     * 聊天消息体
     */
    private String message;
}
