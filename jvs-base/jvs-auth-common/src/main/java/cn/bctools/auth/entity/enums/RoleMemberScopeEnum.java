package cn.bctools.auth.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 角色成员范围
 */
@Getter
@AllArgsConstructor
public enum RoleMemberScopeEnum {

    /**
     * 租户下所有用户
     */
    ALL("ALL", "所有人"),
    /**
     * 指定人员
     */
    USER("USER", "用户"),
    /**
     * 指定部门
     */
    DEPT("DEPT", "部门"),
    ;


    @EnumValue
    @JsonEnumDefaultValue
    public final String value;
    public final String desc;
}
