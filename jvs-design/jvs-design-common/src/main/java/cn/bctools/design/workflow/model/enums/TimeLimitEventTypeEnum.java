package cn.bctools.design.workflow.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 超过审批期限的执行事件类型
 */
@Getter
@AllArgsConstructor
public enum TimeLimitEventTypeEnum {

    /**
     * 审批期限类型
     */
    PASS("PASS", "自动通过"),
    REFUSE("REFUSE", "自动拒绝"),
    NOTIFY("NOTIFY", "发送提醒"),
    ;

    private String value;
    private String desc;
}
