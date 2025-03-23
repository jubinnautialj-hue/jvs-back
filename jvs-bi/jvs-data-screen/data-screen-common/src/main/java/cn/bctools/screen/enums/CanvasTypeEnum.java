package cn.bctools.screen.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 画布类型
 */
@Getter
@AllArgsConstructor
public enum CanvasTypeEnum {

    formal("formal","正式使用"),
    template("template","模板"),
    ;

    @EnumValue
    private final String code;
    private final String desc;
}
