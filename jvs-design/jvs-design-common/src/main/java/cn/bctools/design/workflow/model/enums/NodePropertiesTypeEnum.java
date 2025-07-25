package cn.bctools.design.workflow.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 操作人员类型
 */
@Getter
@AllArgsConstructor
public enum NodePropertiesTypeEnum {

    /**
     * 待审批人类型
     */
    ASSIGN_USER("ASSIGN_USER", "指定人员"),
    SELF_SELECT("SELF_SELECT", "发起人自选"),
    LEADER_TOP("LEADER_TOP", "连续多级主管"),
    LEADER("LEADER", "主管"),
    ROLE("ROLE", "角色"),
    SELF("SELF", "发起人自己"),
    JOB("JOB", "岗位"),
    USER_FIELD("USER_FIELD", "成员字段"),
    DEPT("DEPT", "部门"),
    DEPT_FIELD("DEPT_FIELD", "部门字段"),

    /**
     * 指定抄送人
     */
    ASSIGN_CARBON_COPY("ASSIGN_CARBON_COPY", "指定抄送人"),
    ;

    private String value;
    private String desc;

}
