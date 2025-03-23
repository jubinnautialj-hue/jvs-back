package cn.bctools.report.utils.excel.enums;

import cn.bctools.report.utils.excel.highlight.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EConditionSubType {
    /**
     * 时间日期
     */
    timePeriod(TimePeriodHandler.class),
    /**
     * 唯一值
     */
    uniqueValues(UniqueValuesHandler.class),
    /**
     * 重复值
     */
    duplicateValues(DouplicateValuesHandler.class),
    /**
     * 文本
     */
    text(TextHandler.class),
    /**
     * 最前 最后 平均
     */
    rank(RankHandler.class),
    /**
     * 平均值
     */
    average(AverageHandler.class),
    /**
     * 自定义公式
     */
    formula(FormulaHandler.class),
    /**
     * 数值
     */
    number(NumberHandler.class),
    ;
    private final Class<? extends HighlightCellHandler> hClass;

    public static boolean contains(String typeStr){
        try {
            EConditionSubType.valueOf(typeStr);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
