package cn.bctools.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 系统消息时间单位
 */

@Getter
@AllArgsConstructor
public enum TimeLimitTypeEnum {

    /**
     * 审批期限单位
     */
    HOUR("HOUR", "小时"),
    MINUTE("MINUTE", "分钟"),
    DAY("DAY", "天"),
    ;

    private String value;
    private String desc;
}
