package cn.bctools.data.factory.notice.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: ZhuXiaoKang
 * @Description: 支持的企业微信消息通知类型
 */
@Getter
@AllArgsConstructor
public enum WxEnterpriseNoticeEnum {
    /**
     * 文本消息
     */
    TEXT("TEXT", "文本消息"),
    ;
    @JsonValue
    private final String value;
    private final String desc;
}
