package cn.bctools.report.utils.excel.enums;

import cn.bctools.report.utils.excel.highlight.HighlightCellHandler;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EConditionTimePeriodOperator {

    /**
     * 昨天
     */
    yesterday("AND(TODAY()-1=A1)"),
    /**
     * 今天
     */
    today("AND(TODAY()=A1)"),
    /**
     * 明天
     */
    tomorrow("AND(TODAY()+1=A1)"),
    /**
     * 最近7天
     */
    last7Days("AND(A1>=TODAY()-7, A1<=TODAY())"),
    /**
     * 上周
     */
    lastWeek("AND(A1>=TODAY()-WEEKDAY(TODAY())-6, A1<=TODAY()-WEEKDAY(TODAY()))"),
    /**
     * 本周
     */
    thisWeek("AND(A1>=TODAY()-WEEKDAY(TODAY())+1, A1<=TODAY()-WEEKDAY(TODAY())+7)"),
    /**
     * 下周
     */
    nextWeek("AND(A1>=TODAY()-WEEKDAY(TODAY())+8, A1<=TODAY()-WEEKDAY(TODAY())+14)"),
    /**
     * 上月
     */
    lastMonth("AND(MONTH(A1)=MONTH(TODAY())-1, YEAR(A1)=YEAR(TODAY()))"),
    /**
     * 本月
     */
    thisMonth("AND(MONTH(A1)=MONTH(TODAY()), YEAR(A1)=YEAR(TODAY()))"),
    /**
     * 下月
     */
    nextMonth("AND(MONTH(A1)=MONTH(TODAY())+1, YEAR(A1)=YEAR(TODAY()))"),
    ;

    private final String format;

    public String getFormatAndReplace(String locationName){
        if(StrUtil.isBlank(locationName) || StrUtil.equals(locationName, HighlightCellHandler.defaultLocationName)){
            return this.format;
        }
        return StrUtil.replace(this.format,HighlightCellHandler.defaultLocationName,locationName);
    }
}
