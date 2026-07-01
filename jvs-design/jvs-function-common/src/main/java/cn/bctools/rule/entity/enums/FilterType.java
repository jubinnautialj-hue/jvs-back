package cn.bctools.rule.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author jvs
 */
@Getter
@AllArgsConstructor
public enum FilterType {
    AND("且"),
    OR("或"),
    ;
    private final String desc;
}
