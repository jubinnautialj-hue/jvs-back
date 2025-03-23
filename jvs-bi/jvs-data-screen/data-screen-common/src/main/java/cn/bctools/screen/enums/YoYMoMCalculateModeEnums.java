package cn.bctools.screen.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum YoYMoMCalculateModeEnums {
    /**
     * 同环比类型
     */
    MoMGrowthRate("MoMGrowthRate", "环比"),
    YoYMonthGrowthRate("YoYMonthGrowthRate", "月同比"),
    YoYWeekGrowthRate("YoYWeekGrowthRate", "周同比"),
    YoYYearGrowthRate("YoYYearGrowthRate", "年同比"),
    immobilizationDate("immobilizationDate", "固定时间"),
    dynamicDate("dynamicDate", "动态时间");
    @EnumValue
    String value;
    String desc;
}
