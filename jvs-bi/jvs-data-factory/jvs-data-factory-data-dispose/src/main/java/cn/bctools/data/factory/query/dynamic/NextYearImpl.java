package cn.bctools.data.factory.query.dynamic;

import cn.bctools.data.factory.query.dynamic.dto.DynamicTimeDto;
import cn.bctools.data.factory.query.dynamic.dto.DynamicTimeValue;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import org.springframework.stereotype.Component;

/**
 * 明年 明年第一天到明年最后一天
 *
 * @author xiaohui
 */
@Component
public class NextYearImpl implements DynamicTimeInterFace {
    @Override
    public DynamicTimeValue getValue(DynamicTimeDto dynamicTimeDto) {
        DateTime nowDate = DateUtil.date();
        nowDate = DateUtil.offset(nowDate, DateField.YEAR, 1);
        DateTime startTime = DateUtil.beginOfYear(nowDate);
        DateTime endTime = DateUtil.endOfYear(DateUtil.date());
        return new DynamicTimeValue()
                .setStartTime(startTime)
                .setEndTime(endTime);
    }
}
