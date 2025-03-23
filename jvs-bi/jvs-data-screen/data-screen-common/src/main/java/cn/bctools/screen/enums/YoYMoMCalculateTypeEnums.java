package cn.bctools.screen.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum YoYMoMCalculateTypeEnums {
    PERCENTAGE("PERCENTAGE", "百分比", "/"),
    DIFF("DIFF", "差值", "-");
    @EnumValue
    String value;
    String desc;
    String dorisSql;
}
