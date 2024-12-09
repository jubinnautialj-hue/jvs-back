package cn.bctools.design.workflow.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 条件组运算关系
 */
@Getter
@AllArgsConstructor
public enum ConditionOperatorEnum {

    /**
     * 运算关系
     */
    AND("AND"),
    OR("OR"),
    ;

    private String value;
}
