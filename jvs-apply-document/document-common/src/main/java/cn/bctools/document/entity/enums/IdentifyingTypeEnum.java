package cn.bctools.document.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 权限标识枚举
 *
 * @author xiaohui
 */

@Getter
public enum IdentifyingTypeEnum {
    document("document", "文档权限"),
    library("library", "文库权限");
    @EnumValue
    public final String value;
    public final String desc;

    IdentifyingTypeEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
