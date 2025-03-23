package cn.bctools.data.factory.notice.enums;

import cn.bctools.auth.api.enums.PersonnelTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: ZhuXiaoKang
 * @Description: 接收人——人员选择类型
 */
@Getter
@AllArgsConstructor
public enum ReceiverTypeEnum {

    /**
     * 用户
     */
    user(PersonnelTypeEnum.user.getValue()),
    /**
     * 角色
     */
    role(PersonnelTypeEnum.role.getValue()),
    /**
     * 部门
     */
    dept(PersonnelTypeEnum.dept.getValue()),
    /**
     * 用户组
     */
    group(PersonnelTypeEnum.group.getValue()),
    /**
     * 岗位
     */
    job(PersonnelTypeEnum.job.getValue()),
    ;
    private final String value;
}
