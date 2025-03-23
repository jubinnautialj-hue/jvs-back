package cn.bctools.data.factory.query.dynamic;

import cn.bctools.data.factory.query.dynamic.dto.DynamicTimeDto;
import cn.bctools.data.factory.query.dynamic.dto.DynamicTimeValue;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import org.springframework.stereotype.Component;

/**
 * 下季度 下季度第一天到下季度最后一天
 *
 * @author xiaohui
 */
@Component
public class NextQuarterImpl implements DynamicTimeInterFace {
    @Override
    public DynamicTimeValue getValue(DynamicTimeDto dynamicTimeDto) {
        DateTime nowDate = DateUtil.date();
        DateTime endTime = DateUtil.endOfYear(nowDate);
        //本季度的结束时间下一个月就是下一季度
        nowDate = DateUtil.offsetMonth(endTime, 1);
        DateTime startTime = DateUtil.beginOfQuarter(nowDate);
        endTime = DateUtil.endOfQuarter(DateUtil.date());
        return new DynamicTimeValue()
                .setStartTime(startTime)
                .setEndTime(endTime);
    }
}
