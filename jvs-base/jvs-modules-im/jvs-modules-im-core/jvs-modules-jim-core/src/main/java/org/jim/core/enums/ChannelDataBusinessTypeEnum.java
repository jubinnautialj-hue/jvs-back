package org.jim.core.enums;


/**
 * @author ZhuXiaoKang
 * @Description 业务类型枚举
 */
public enum ChannelDataBusinessTypeEnum {

    /**
     * 组相关业务
     */
    GROUP_USER_IDS("groupUserIds", "组内用户id集合"),
    GROUP_USER_INFO("groupUserInfo", "组用户信息Hash")
    ;

    private final String value;
    private final String desc;

    ChannelDataBusinessTypeEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }

    public static ChannelDataBusinessTypeEnum getByValue(String value) {
        for(ChannelDataBusinessTypeEnum currentEnum : ChannelDataBusinessTypeEnum.values()){
            if(value.equals(currentEnum.value)){
                return currentEnum;
            }
        }
        return null;
    }
}
