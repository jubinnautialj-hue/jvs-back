package cn.bctools.design.project.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 应用版本状态枚举
 */
@Getter
@AllArgsConstructor
public enum AppVersionStatusEnum {
    /**
     * 历史版本
     */
    HISTORY("HISTORY"),
    /**
     * 正在使用的版本
     */
    USE("USE"),
    /**
     * 暂停使用的版本(只有正式版本会有这个状态)
     */
    SUSPEND("SUSPEND"),
    ;

    @EnumValue
    @JsonValue
    private String value;
}
