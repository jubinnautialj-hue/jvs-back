package cn.bctools.im.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * @author ZhuXiaoKang
 * @Description 数据删除状态枚举
 */
public enum DelFlagEnum {

    /**
     * 正常
     */
    NORMAL(0, "正常"),
    /**
     * 删除
     */
    DELETED(1, "删除");

    @EnumValue
    private final int value;
    private final String desc;

    DelFlagEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public static DelFlagEnum getByValue(int value) {
        for(DelFlagEnum currentEnum : DelFlagEnum.values()){
            if(value == currentEnum.value){
                return currentEnum;
            }
        }
        return null;
    }
}
