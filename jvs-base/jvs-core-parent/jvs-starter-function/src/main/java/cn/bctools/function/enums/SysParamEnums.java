package cn.bctools.function.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统参数
 *
 * @Author: GuoZi
 */
@Getter
@AllArgsConstructor
public enum SysParamEnums {

    /**
     * 系统参数
     */
    UserId("userId", "用户id", "当前用户的id", JvsParamType.text),
    RealName("RealName", "用户昵称", "当前用户的昵称", JvsParamType.text),
    AccountName("AccountName", "用户帐号名", "当前用户的帐号名", JvsParamType.text),
    Phone("Phone", "用户手机号", "当前用户的手机号", JvsParamType.text),
    Email("Email", "用户邮箱", "当前用户的邮箱", JvsParamType.text),
    HeadImg("HeadImg", "用户头像", "当前用户的头像图片链接", JvsParamType.text),
    DeptId("DeptId", "用户部门ID", "当前用户所在的部门ID", JvsParamType.array),
    //    UserAccount("UserAccount", "用户账号", "当前用户账号", JvsParamType.text),
    UserEmployeeNo("UserEmployeeNo", "用户职工编号", "当前用户职工编号", JvsParamType.text),
    UserRole("UserRole", "用户角色", "当前用户角色", JvsParamType.array),
    DeptName("DeptName", "用户部门名称", "当前用户所在的部门名称", JvsParamType.array),
    DeptCode("DeptCode", "用户部门编码", "当前用户所在的部门编码", JvsParamType.array),
    JobName("JobName", "用户岗位名称", "当前用户所在的岗位名称", JvsParamType.text),
    TenantCode("TenantCode", "用户登录租户", "当前用户登录租户", JvsParamType.text),
    ;

    private final String id;
    private final String name;
    private final String info;
    private final JvsParamType jvsParamType;

    public static SysParamEnums getById(String id) {
        if (id == null) {
            return null;
        }
        for (SysParamEnums value : SysParamEnums.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }

}
