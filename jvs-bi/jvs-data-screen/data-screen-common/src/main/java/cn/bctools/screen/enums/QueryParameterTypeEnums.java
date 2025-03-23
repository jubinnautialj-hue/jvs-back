package cn.bctools.screen.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QueryParameterTypeEnums {
    //输入 年月日 年月日范围 范围筛选  用户信息 角色信息 部门信息 岗位信息 群组信息
    input, date, datetimerange, numberRange, user, role, dept, job, group,select;
}
