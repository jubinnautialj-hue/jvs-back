package cn.bctools.design.workflow.model.enums;

import cn.bctools.auth.api.enums.PersonnelTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 人员对象类型
 */
@Getter
@AllArgsConstructor
public enum TargetObjectTypeEnum {

    /**
     * 用户
     */
    user(PersonnelTypeEnum.user.getValue()),
    /**
     * 部门
     */
    dept(PersonnelTypeEnum.dept.getValue()),
    /**
     * 角色
     */
    role(PersonnelTypeEnum.role.getValue()),
    /**
     * 岗位
     */
    job(PersonnelTypeEnum.job.getValue()),
    /**
     * 成员字段
     */
    user_field("user_field"),
    /**
     * 部门字段
     */
    dept_field("dept_field"),
    ;

    private final String value;
}
