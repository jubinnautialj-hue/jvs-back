package cn.bctools.chart.chart.compare.unit;

import cn.bctools.chart.enums.YoYMoMUnitEnums;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;

/**
 * @author Administrator
 * 同环比
 */
public interface YoYMoMUnitSql {

    /**
     * 获取不同时间格式 处理的单位
     *
     * @param dataFieldTypeEnum 字段类型
     * @param value             不同日期格式相差的值
     * @param pursuantKey       依据key
     */
    default String functionSql(DataFieldTypeEnum dataFieldTypeEnum, Object value, String pursuantKey, YoYMoMUnitEnums yoYMoMUnitEnums) {
        return String.format(yoYMoMUnitEnums.getDorisFunction(), "a.`" + pursuantKey + "`", value);
    }


}
