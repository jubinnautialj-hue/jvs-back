package cn.bctools.auth.api.dto;

/**
 * @author jvs The enum Dept rang type dto.
 */
public enum DeptRangTypeDto {
    /**
     * Current dept rang type dto.
     */
    current("当前部门及以下"),
    /**
     * Samelevel dept rang type dto.
     */
    samelevel("同级部门及以下"),
    /**
     * Dept dept rang type dto.
     */
    dept("指定部门及以下"),
    ;

    /**
     * The Msg.
     */
    public String msg;

    DeptRangTypeDto(String msg) {
        this.msg = msg;
    }
}
