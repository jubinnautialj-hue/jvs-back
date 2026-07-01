package cn.bctools.design.workflow.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 终止任务类型
 */
@Getter
@AllArgsConstructor
public enum TerminatedTypeEnum {

    /**
     * 发起人撤回
     */
    WITHDRAW("WITHDRAW", "发起人撤回"),
    /**
     * 任务终止
     */
    TERMINATION("TERMINATION","终止"),
    ;

    @JsonValue
    private String value;
    private String desc;
}
