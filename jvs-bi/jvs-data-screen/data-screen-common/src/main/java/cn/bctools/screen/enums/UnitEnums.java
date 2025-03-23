package cn.bctools.screen.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UnitEnums {
    none("none", "无"),
    perCent("perCent", "%"),
    pt("pt", "百分号简写"),
    thousandsSeparator("thousandsSeparator", "千分位分隔符");

    @EnumValue
    String value;
    String desc;

}
