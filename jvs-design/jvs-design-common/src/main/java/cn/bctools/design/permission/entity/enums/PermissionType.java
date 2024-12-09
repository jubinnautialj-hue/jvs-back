package cn.bctools.design.permission.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 权限类型
 */

@Getter
@AllArgsConstructor
public enum PermissionType {
    /**
     * 操作权限
     */
    OPERATION("OPERATION", "操作权限"),
    /**
     * 数据权限
     */
    DATA("DATA", "数据权限"),
    ;

    @EnumValue
    @JsonValue
    private String value;
    private String desc;

}
