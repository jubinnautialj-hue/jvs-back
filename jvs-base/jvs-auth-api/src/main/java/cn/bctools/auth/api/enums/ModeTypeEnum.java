package cn.bctools.auth.api.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author zhuxiaokang
 * 应用版本类型枚举
 */
@Getter
@AllArgsConstructor
public enum ModeTypeEnum {
    /**
     * 开发版
     */
    DEV("DEV","开发模式"),
    /**
     * 测试版
     */
    BETA("BETA","测试模式"),
    /**
     * 正式版
     */
    GA("GA","正式模式"),
    ;

    @JsonValue
    private String value;
    private String msg;

    /**
     * 获取版本类型
     *
     * @param value 值
     * @return 类型
     */
    public static ModeTypeEnum getType(String value) {
        return Arrays.stream(ModeTypeEnum.values())
                .filter(v -> v.getValue().equals(value))
                .findFirst()
                .orElse(ModeTypeEnum.GA);
    }
    public static ModeTypeEnum getMsgType(String msg) {
        return Arrays.stream(ModeTypeEnum.values())
                .filter(v -> v.getMsg().equals(msg))
                .findFirst()
                .orElse(ModeTypeEnum.GA);
    }
}

