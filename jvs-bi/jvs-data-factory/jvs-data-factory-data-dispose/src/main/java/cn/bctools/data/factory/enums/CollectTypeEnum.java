package cn.bctools.data.factory.enums;

import cn.bctools.data.factory.doris.condition.*;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 计数，还是去重计数
 *
 * @author guojing
 */
@AllArgsConstructor
@Getter
public enum CollectTypeEnum {
    /**
     * 求和
     */
    sum("sum", "求和", SumDorisCollect.class),
    /**
     * 计数
     */
    count("count", "计数", CountDorisCollect.class),
    /**
     * 平均值
     */
    avg("avg", "平均值", AvgDorisCollect.class),
    /**
     * 去重计算
     */
    countDistinct("countDistinct", "去重计算", DistinctCountDorisCollect.class),
    /**
     * 最大值
     */
    max("max", "最大值", MaxDorisCollect.class),
    /**
     * 最小值
     */
    min("min", "最小值", MinDorisCollect.class),
    /**
     * 不计算
     */
    notCalculate("notCalculate", "不计算", NotCalculateDorisCollect.class),
    ;
    @EnumValue
    String value;
    String desc;
    Class<? extends DorisCollectCondition> dorisClass;
}
