package cn.bctools.design.notice.handler.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 */
@Getter
@AllArgsConstructor
public enum TriggerEditedConditionTypeEnum {

    // 任意字段
    ANY("any"),
    // 指定字段
    SPECIFIED_FIELD("specified_field"),
    ;

    @JsonValue
    private final String value;
}
