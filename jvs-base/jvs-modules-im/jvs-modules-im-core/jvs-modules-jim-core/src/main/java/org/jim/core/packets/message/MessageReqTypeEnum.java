package org.jim.core.packets.message;

/**
 * @author ZhuXiaoKang
 * @Description 查询消息类型枚举
 * @createTime 2021/03/30 16:26
 */
public enum MessageReqTypeEnum {

    /**
     * 离线聊天消息
     */
    CHAT_OFFLINE(0, "离线聊天消息"),
    /**
     * 历史聊天消息
     */
    CHAT_HISTORY(1, "历史聊天消息"),
    /**
     * 离线通知消息
     */
    NOTIFY_OFFLINE(2, "离线通知消息"),
    /**
     * 历史通知消息
     */
    NOTIFY_HISTORY(3, "历史通知消息"),
    /**
     * 业务消息
     */
    BUSINESS(4, "业务消息");

    private final int value;
    private final String desc;

    MessageReqTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public static MessageReqTypeEnum getByValue(int value) {
        for(MessageReqTypeEnum currentEnum : MessageReqTypeEnum.values()){
            if(value == currentEnum.value){
                return currentEnum;
            }
        }
        return null;
    }

}
