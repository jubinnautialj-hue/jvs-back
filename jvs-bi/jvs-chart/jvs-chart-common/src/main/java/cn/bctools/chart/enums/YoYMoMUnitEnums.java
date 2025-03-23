package cn.bctools.chart.enums;

import cn.bctools.chart.chart.compare.unit.YoYMoMUnitSql;
import cn.bctools.chart.chart.compare.unit.impl.YoYMoMDayUnitSqlImpl;
import cn.bctools.chart.chart.compare.unit.impl.YoYMoMMonthUnitSqlImpl;
import cn.bctools.chart.chart.compare.unit.impl.YoYMoMWeeksUnitSqlImpl;
import cn.bctools.chart.chart.compare.unit.impl.YoYMoMYearUnitSqlImpl;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum YoYMoMUnitEnums {
    /**
     * 注意 这里的规则是根据 字段类型来的  例如 day 枚举类型 他会根据具体的字段类型 计算规则也不同 具体可以看看 实现类
     */
    DAY("DAY", "天", "DAYS_SUB(%s,%s)", YoYMoMDayUnitSqlImpl.class),
    WEEK("WEEKS", "周", "WEEKS_SUB(%s,%s)", YoYMoMWeeksUnitSqlImpl.class),
    MONTH("MONTH", "月", "MONTHS_SUB(%s,%s)", YoYMoMMonthUnitSqlImpl.class),
    YEAR("YEAR", "年", "YEARS_SUB(%s,%s)", YoYMoMYearUnitSqlImpl.class);

    @EnumValue
    String value;
    String desc;
    String dorisFunction;
    /**
     * 获取doris函数 sql 因为这里存在自定义时间格式 这种格式的日期需要特殊处理
     */
    Class<? extends YoYMoMUnitSql> getFunctionClass;

}