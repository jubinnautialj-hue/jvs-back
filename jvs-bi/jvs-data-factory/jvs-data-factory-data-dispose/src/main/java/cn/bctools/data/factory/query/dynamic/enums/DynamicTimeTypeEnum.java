package cn.bctools.data.factory.query.dynamic.enums;

import cn.bctools.data.factory.query.dynamic.*;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xiaohui
 */

@Getter
@AllArgsConstructor
public enum DynamicTimeTypeEnum {
    last7Days("last7Days", "近7天", Last7DaysImpl.class),
    last30Days("last30Days", "近30天", Last30DaysImpl.class),
    thisDay("thisDay", "今天", ThisDayImpl.class),
    lastDay("lastDay", "昨天", LastDayImpl.class),
    nextDay("nextDay", "明天", NextDayImpl.class),
    thisYear("thisYear", "今年", ThisYearImpl.class),
    lastYear("lastYear", "去年", LastYearImpl.class),
    nextYear("nextYear", "明年", NextYearImpl.class),
    thisMonth("thisMonth", "本月", ThisMonthImpl.class),
    lastMonth("lastMonth", "上月", LastMonthImpl.class),
    nextMonth("nextMonth", "下月", NextMonthImpl.class),
    thisQuarter("thisQuarter", "本季度", ThisQuarterImpl.class),
    lastQuarter("lastQuarter", "上季度", LastQuarterImpl.class),
    //    custom("custom","自定义",),
    nextQuarter("nextQuarter", "下季度", NextQuarterImpl.class);
    @EnumValue
    String value;
    String desc;
    Class<? extends DynamicTimeInterFace> cls;
}
