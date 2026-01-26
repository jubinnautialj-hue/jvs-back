package cn.bctools.permission.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色人员类型
 *
 * @author guojing
 */
@Getter
@AllArgsConstructor
public enum AuthRolePersonType {
    /**
     * 全部
     */
    all,
    /**
     * 自定义
     */
    custom
}
