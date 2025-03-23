package cn.bctools.data.factory.query.dynamic;

import cn.bctools.data.factory.query.dynamic.dto.DynamicTimeDto;
import cn.bctools.data.factory.query.dynamic.dto.DynamicTimeValue;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import org.springframework.stereotype.Component;

/**
 * 近7天 因为包含当天所以倒退时间应该减一
 *
 * @author xiaohui
 */
@Component
public class Last7DaysImpl implements DynamicTimeInterFace {
    @Override
    public DynamicTimeValue getValue(DynamicTimeDto dynamicTimeDto) {
        DateTime nowDate = DateUtil.date();
        nowDate = DateUtil.offsetDay(nowDate, -6);
        DateTime startTime = DateUtil.beginOfDay(nowDate);
        DateTime endTime = DateUtil.endOfDay(DateUtil.date());
        return new DynamicTimeValue()
                .setStartTime(startTime)
                .setEndTime(endTime);
    }
}
