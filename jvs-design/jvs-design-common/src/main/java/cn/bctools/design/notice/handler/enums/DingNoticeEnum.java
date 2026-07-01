package cn.bctools.design.notice.handler.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 支持的钉钉消息通知类型
 */
@Getter
@AllArgsConstructor
public enum DingNoticeEnum {
    /**文本消息*/
    TEXT("TEXT", "文本消息"),
    /**LinkMessage*/
    LinkMessage("LinkMessage", "LinkMessage"),
    ;
    @JsonValue
    private final String value;
    private final String desc;
}
