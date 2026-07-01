package cn.bctools.design.project.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
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
public enum AppVersionTypeEnum {
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

    @EnumValue
    @JsonValue
    private String value;
    private String msg;

    /**
     * 获取版本类型
     *
     * @param value 值
     * @return 类型
     */
    public static AppVersionTypeEnum getType(String value) {
        return Arrays.stream(AppVersionTypeEnum.values())
                .filter(v -> v.getValue().equals(value))
                .findFirst()
                .orElse(AppVersionTypeEnum.GA);
    }
    public static AppVersionTypeEnum getMsgType(String msg) {
        return Arrays.stream(AppVersionTypeEnum.values())
                .filter(v -> v.getMsg().equals(msg))
                .findFirst()
                .orElse(AppVersionTypeEnum.GA);
    }
}
