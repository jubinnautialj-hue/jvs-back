package cn.bctools.report.enums;

import cn.bctools.report.render.functions.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EStatsType {

    /*只有数字类型才有*/
    SUM("求和", FSum.class),
    MAX("最大值", FMax.class),
    MIN("最小值", FMin.class),
    AVG("平均值", FAvg.class),

    /*所有的类型都有*/
    COUNT("计数",FCount.class),
    DISTINCT_COUNT("去重计数",FDISCount.class),
    ;

    private final String desc;
    private final Class<? extends FStats> fClass;
}
