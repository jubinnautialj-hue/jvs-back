package cn.bctools.design.workflow.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 人员选择范围类型枚举
 */

@Getter
@AllArgsConstructor
public enum PersonnelScopeTypeEnum {
    /**
     * 组织的所有人
     */
    ALL("ALL", "组织的所有人"),
    /**
     * 自定义范围
     */
    CUSTOM("CUSTOM", "自定义范围"),
    ;

    private String value;
    private String desc;
}
