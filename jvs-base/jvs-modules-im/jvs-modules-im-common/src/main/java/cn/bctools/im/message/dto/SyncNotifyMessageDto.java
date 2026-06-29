package cn.bctools.im.message.dto;

import lombok.Data;

/**
 * @author ZhuXiaoKang
 * @Description 持久化通知消息rabbit dto
 */

@Data
public class SyncNotifyMessageDto extends BaseSyncMessageDto {

    /**
     * 0-广播，1-组通知，2-精确通知
     */
    private Integer notifyType;

    /**
     * 发送消息用户id(若是系统发送，则默认为0)
     */
    private String fromUserId;

    /**
     * 目标群组id(JSON数组)
     */
    private String groupIds;

    /**
     * 目标用户id(JSON数组)
     */
    private String userIds;

    /**
     * 目标租户id(JSON数组)
     */
    private String tenantIds;

    /**
     * 聊天消息体
     */
    private String message;

}
