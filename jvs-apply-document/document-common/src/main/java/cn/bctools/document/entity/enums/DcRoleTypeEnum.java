package cn.bctools.document.entity.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 角色类型
 */
@Getter
public enum DcRoleTypeEnum {
    /**
     * 所有者系统角色不能删除
     */
    owner("owner", "所有者"),
    /**
     * 用户创建的数据类型
     */
    user("user", "用户创建");
    @EnumValue
    public final String value;
    public final String desc;

    DcRoleTypeEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}

