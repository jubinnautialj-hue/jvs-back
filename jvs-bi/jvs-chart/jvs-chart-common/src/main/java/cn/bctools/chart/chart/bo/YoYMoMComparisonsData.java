package cn.bctools.chart.chart.bo;


import cn.bctools.chart.enums.YoYMoMCalculateModeEnums;
import cn.bctools.chart.enums.YoYMoMCalculateTypeEnums;
import cn.bctools.chart.enums.YoYMoMUnitEnums;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 同环比
 */
@Data
@Accessors(chain = true)
public class YoYMoMComparisonsData extends DataSourceField {

    /**
     * 别名
     */
    private String aliasName;

    /**
     * 计算方式
     */
    private YoYMoMCalculateModeEnums calculateMode;

    /**
     * 计算类型
     */
    private YoYMoMCalculateTypeEnums calculateType;
    /**
     * 依据key
     */
    private String pursuantKey;
    /**
     * 依据key 的类型
     */
    private DataFieldTypeEnum pursuantFieldType;

    /**
     * 单位  例如是天减n还是周，年，月
     */
    private YoYMoMUnitEnums unit;
    /**
     * 具体值 如果是固定时间(yyyy-MM-dd,yyyy-MM-dd HH:mm:ss.SS)就是时间字符串 如果是其他就是数字
     */
    private Object value;
    /**
     * 小数位数
     */
    private Integer decimals;

}
