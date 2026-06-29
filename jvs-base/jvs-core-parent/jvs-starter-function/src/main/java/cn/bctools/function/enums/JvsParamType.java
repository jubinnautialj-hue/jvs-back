package cn.bctools.function.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Jvs函数: 值类型
 *
 * @Author: GuoZi
 */
@Getter
@AllArgsConstructor
public enum JvsParamType {

    /**
     * 值类型
     */
    text("text", "文本", String.class),
    number("number", "数字", Number.class),
    date("date", "日期", Date.class),
    bool("bool", "布尔", Boolean.class),
    array("array", "数组", List.class),
    object("object", "对象", Map.class),
    file("file", "文件", Object.class),
    any("any", "任何类型", Object.class),
    /**
     * NULL值
     */
    none("none", "空", null);;
    @EnumValue
    @JsonValue
    private final String value;
    @ApiModelProperty("描述")
    private final String desc;
    @ApiModelProperty("Java类型")
    private final Class<?> aClass;

    public static JvsParamType desc(String desc) {
        for (JvsParamType value : JvsParamType.values()) {
            if (value.desc.equals(desc)) {
                return value;
            }
        }
        return JvsParamType.any;
    }

    public static JvsParamType getByClass(Class<?> aClass) {
        if (aClass == null) {
            return null;
        }
        for (JvsParamType value : JvsParamType.values()) {
            if (value.getAClass().isAssignableFrom(aClass)) {
                return value;
            }
        }
        return JvsParamType.any;
    }

}
