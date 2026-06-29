package cn.bctools.message.push.dto.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xh
 */

@Getter
@AllArgsConstructor
public enum MessagePushStatusEnum {
    /**
     * 未开始
     */
    WAIT("WAIT", "未开始"),
    /**
     * 成功
     */
    SUCCESS("SUCCESS", "成功"),
    /**
     * 失败
     */
    FAILED("FAILED", "失败");

    @EnumValue
    private final String value;
    private final String desc;
}
