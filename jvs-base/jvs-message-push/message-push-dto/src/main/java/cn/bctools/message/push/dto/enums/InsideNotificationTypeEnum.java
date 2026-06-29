package cn.bctools.message.push.dto.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author czy
 */
@Getter
@AllArgsConstructor
public enum InsideNotificationTypeEnum {
    /**
     * 通知
     */
    notice("notice", "通知"),
    /**
     * 项目
     */
    project("project", "项目");

    @EnumValue
    private final String code;
    private final String desc;

}
