package cn.bctools.im.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * @author ZhuXiaoKang
 * @Description IM消息类型枚举
 */
public enum MessageTypeEnum {

    /**
     * 群聊
     */
    CHAT_GROUP(1, "群聊"),
    /**
     * 私聊
     */
    CHAT_PRIVATE(2, "私聊"),
    /**
     * 通知
     */
    NOTIFY(3, "通知");

    @EnumValue
    private final int value;
    private final String desc;

    MessageTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public static MessageTypeEnum getByValue(int value) {
        for(MessageTypeEnum currentEnum : MessageTypeEnum.values()){
            if(value == currentEnum.value){
                return currentEnum;
            }
        }
        return null;
    }
}
