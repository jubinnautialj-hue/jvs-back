package cn.bctools.design.permission.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 为更方便的授权整个应用的权限。
 * 应用的权限组类型，功能权限，针对界面的授权功能 ： 分配哪些功能给某一个角色或部门
 * 应用的数据权限，模型的数据权限：分配哪些模型的数据权限给某一个角色或部门
 *
 * @author jvs
 */
@Getter
@AllArgsConstructor
public enum PermissionGroupType {
    /**
     * 设计权限组
     */
    DESIGN("DESIGN", "设计权限组"),
    /**
     * 模型权限组
     */
    MODEL("MODEL", "模型权限组"),
    ;

    @EnumValue
    @JsonValue
    private String value;
    private String desc;
}
