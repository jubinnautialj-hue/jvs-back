package cn.bctools.data.factory.enums;

import cn.bctools.data.factory.query.interval.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IntervalTypeEnum {
    OpenInterval("开区间","()", IntervalOpen.class),
    ClosedInterval("闭区间","[]", IntervalClosed.class),
    HalfOpenHalfClosed("左开右闭","(]", IntervalHalfOpenHalfClosed.class),
    HalfClosedHalfOpen("左闭右开","[)", IntervalHalfClosedHalfOpen.class),
    ;

    private final String desc;
    private final String sign;
    private final Class<? extends IntervalBase> aClass;
}
