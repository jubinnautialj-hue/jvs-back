package cn.bctools.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户搜索维度
 *
 * @author Administrator
 */
@Getter
@AllArgsConstructor
public enum SearchType {
    /**
     * 用户
     */
    user,
    /**
     * 角色
     */
    role,
    /**
     * 部门
     */
    dept,
    /**
     * 岗位
     */
    job,
    /**
     * 分组
     */
    group;
}
