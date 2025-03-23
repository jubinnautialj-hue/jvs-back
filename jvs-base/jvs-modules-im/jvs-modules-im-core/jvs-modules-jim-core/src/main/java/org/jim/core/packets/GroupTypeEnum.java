package org.jim.core.packets;

/**
 * 组类型
 */
public enum GroupTypeEnum {

    // 普通组
    DEFAULT(0),
    // 文档消息组
    BUSINESS(1),
    ;

    private final int value;

    public final int getNumber() {
        return value;
    }

    GroupTypeEnum(int value) {
        this.value = value;
    }

    public static GroupTypeEnum valueOf(int value) {
        return forNumber(value);
    }

    public static GroupTypeEnum forNumber(int value) {
        for(GroupTypeEnum currentEnum : GroupTypeEnum.values()){
            if(value == currentEnum.value){
                return currentEnum;
            }
        }
        return null;
    }
}

