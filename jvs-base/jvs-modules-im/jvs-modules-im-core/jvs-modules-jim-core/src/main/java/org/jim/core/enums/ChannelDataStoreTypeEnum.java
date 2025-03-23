package org.jim.core.enums;


/**
 * @author ZhuXiaoKang
 * @Description 存储方式枚举
 */
public enum ChannelDataStoreTypeEnum {

    /**
     * redis存储
     */
    REDIS("Redis"),
    ;

    private final String value;

    ChannelDataStoreTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ChannelDataStoreTypeEnum getByValue(String value) {
        for(ChannelDataStoreTypeEnum currentEnum : ChannelDataStoreTypeEnum.values()){
            if(value.equals(currentEnum.value)){
                return currentEnum;
            }
        }
        return null;
    }
}
