package cn.bctools.design.workflow.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 会签规则枚举
 */
@Getter
@AllArgsConstructor
public enum CountersignRuleEnum {
    /**
     * 按比例
     */
    RATIO("RATIO", "按比例"),
    ;

    private String value;
    private String desc;
}
