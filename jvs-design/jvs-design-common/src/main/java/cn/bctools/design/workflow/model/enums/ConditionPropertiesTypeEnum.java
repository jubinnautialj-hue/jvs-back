package cn.bctools.design.workflow.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 条件类型
 */
@Getter
@AllArgsConstructor
public enum ConditionPropertiesTypeEnum {

    /**
     * 条件类型
     */
    ORG("org", "发起人"),
    FUN("fun", "公式"),
    ;

    private String value;
    private String desc;
}
