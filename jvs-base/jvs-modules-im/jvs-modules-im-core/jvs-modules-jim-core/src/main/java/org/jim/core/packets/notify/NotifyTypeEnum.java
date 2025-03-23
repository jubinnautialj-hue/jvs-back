package org.jim.core.packets.notify;

/**
 * @author ZhuXiaoKang
 * @Description 通知类型枚举
 */
public enum NotifyTypeEnum {

    // 广播
    NOTIFY_TYPE_BROADCAST(0),
    // 组推送
    NOTIFY_TYPE_GROUP(1),
    // 精确推送
    NOTIFY_TYPE_ACCURATE(2);

    private final int value;

    public final int getNumber() {
        return value;
    }

    NotifyTypeEnum(int value) {
        this.value = value;
    }

    public static NotifyTypeEnum valueOf(int value) {
        return forNumber(value);
    }

    public static NotifyTypeEnum forNumber(int value) {
        for(NotifyTypeEnum currentEnum : NotifyTypeEnum.values()){
            if(value == currentEnum.value){
                return currentEnum;
            }
        }
        return null;
    }

}
