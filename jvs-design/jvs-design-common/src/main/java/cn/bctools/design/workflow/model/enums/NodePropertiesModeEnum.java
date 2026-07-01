package cn.bctools.design.workflow.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 审批模式
 */
@Getter
@AllArgsConstructor
public enum NodePropertiesModeEnum {

    /**
     * 默认审批模式
     */
    DEFAULT("DEFAULT", ""),
    /**
     * 可同时审批
     */
    AND("AND", "会签"),
    /**
     * 依次审批
     */
    NEXT("NEXT", "按选择顺序依次审批"),
    /**
     * 一票通过（有一人同意即可）
     */
    OR("OR", "或签"),
    ;

    private String value;
    private String desc;
}

