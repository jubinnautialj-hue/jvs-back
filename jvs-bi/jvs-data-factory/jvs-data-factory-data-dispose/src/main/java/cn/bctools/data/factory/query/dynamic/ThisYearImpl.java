package cn.bctools.data.factory.query.dynamic;

import cn.bctools.data.factory.query.dynamic.dto.DynamicTimeDto;
import cn.bctools.data.factory.query.dynamic.dto.DynamicTimeValue;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import org.springframework.stereotype.Component;

/**
 * 今年 今年第一天到今年最后一天
 *
 * @author xiaohui
 */
@Component
public class ThisYearImpl implements DynamicTimeInterFace {
    @Override
    public DynamicTimeValue getValue(DynamicTimeDto dynamicTimeDto) {
        DateTime nowDate = DateUtil.date();
        DateTime startTime = DateUtil.beginOfYear(nowDate);
        DateTime endTime = DateUtil.endOfYear(nowDate);
        return new DynamicTimeValue()
                .setStartTime(startTime)
                .setEndTime(endTime);
    }
}
