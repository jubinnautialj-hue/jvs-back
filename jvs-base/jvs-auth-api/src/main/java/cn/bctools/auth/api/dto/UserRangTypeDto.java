package cn.bctools.auth.api.dto;

/**
 * @author jvs The enum User rang type dto.
 */
public enum UserRangTypeDto {
    /**
     * Current dept user rang type dto.
     */
    currentDept("当前部门"),
    /**
     * Dept user rang type dto.
     */
    dept("指定部门"),
    /**
     * User user rang type dto.
     */
    user("指定人员"),
    /**
     * Job user rang type dto.
     */
    job("指定岗位"),
    /**
     * Role user rang type dto.
     */
    role("指定角色"),
    ;

    /**
     * The Msg.
     */
    public String msg;

    UserRangTypeDto(String msg) {
        this.msg = msg;
    }
}
