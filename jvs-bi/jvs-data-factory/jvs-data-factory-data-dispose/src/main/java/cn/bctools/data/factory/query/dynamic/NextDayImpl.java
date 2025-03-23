package cn.bctools.data.factory.query.dynamic;

import cn.bctools.data.factory.query.dynamic.dto.DynamicTimeDto;
import cn.bctools.data.factory.query.dynamic.dto.DynamicTimeValue;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import org.springframework.stereotype.Component;

/**
 * 明天 明天开始时间到明天结束时间
 *
 * @author xiaohui
 */
@Component
public class NextDayImpl implements DynamicTimeInterFace {
    @Override
    public DynamicTimeValue getValue(DynamicTimeDto dynamicTimeDto) {
        DateTime nowDate = DateUtil.date();
        nowDate = DateUtil.offsetDay(nowDate, 1);
        DateTime startTime = DateUtil.beginOfDay(nowDate);
        DateTime endTime = DateUtil.endOfDay(DateUtil.date());
        return new DynamicTimeValue()
                .setStartTime(startTime)
                .setEndTime(endTime);
    }
}
