package cn.bctools.auth.login.auth.ding;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 钉钉API
 */
@Getter
@AllArgsConstructor
public enum DingApiEnum {

    /**
     * 获取下一级部门基础信息
     */
    GET_DEPARTMENT_ALL_URL("https://oapi.dingtalk.com/topapi/v2/department/listsub"),

    /***
     * 根据部门ID获取指定部门详情
     */
    GET_DEPARTMENT_DETAIL_URL("https://oapi.dingtalk.com/topapi/v2/department/get"),

    /**
     * 获取指定部门中的用户详细信息
     */
    DEPT_USER_LIST_URL("https://oapi.dingtalk.com/topapi/v2/user/list"),
    ;

    private final String url;

}
