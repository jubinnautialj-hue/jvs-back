package cn.bctools.data.factory.query.dynamic;

import cn.bctools.data.factory.query.dynamic.dto.DynamicTimeDto;
import cn.bctools.data.factory.query.dynamic.dto.DynamicTimeValue;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import org.springframework.stereotype.Component;

/**
 * 上季度 上季度第一天到上季度最后一天
 *
 * @author xiaohui
 */
@Component
public class LastQuarterImpl implements DynamicTimeInterFace {
    @Override
    public DynamicTimeValue getValue(DynamicTimeDto dynamicTimeDto) {
        DateTime nowDate = DateUtil.date();
        DateTime startTime = DateUtil.beginOfQuarter(nowDate);
        //本季度的开始时间上一个月就是上一季度
        nowDate = DateUtil.offsetMonth(startTime, -1);
        startTime = DateUtil.beginOfQuarter(nowDate);
        DateTime endTime = DateUtil.endOfQuarter(DateUtil.date());
        return new DynamicTimeValue()
                .setStartTime(startTime)
                .setEndTime(endTime);
    }
}
