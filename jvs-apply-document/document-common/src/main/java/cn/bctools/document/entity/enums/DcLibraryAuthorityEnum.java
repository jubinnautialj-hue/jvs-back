package cn.bctools.document.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * @Author: ZhuXiaoKang
 * @Description: 知识库权限-权限
 */
public enum DcLibraryAuthorityEnum {

    /**
     * 知识库权限
     */
    read("read","查看"),
    write("write","编辑"),
    possess("possess","所有者"),
    ;
    @EnumValue
    public final String value;
    public final String desc;

    DcLibraryAuthorityEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

}
