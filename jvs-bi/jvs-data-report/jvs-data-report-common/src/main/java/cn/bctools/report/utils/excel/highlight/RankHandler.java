package cn.bctools.report.utils.excel.highlight;

import cn.bctools.report.model.univer.style.UStyle;
import cn.bctools.report.utils.ExcelUtils;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFSheetConditionalFormatting;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@Slf4j
public class RankHandler implements HighlightCellHandler{

    private final static String BOTTOM = "AND({}<=SMALL({},{}),AND(NOT(ISBLANK({})), LEN(TRIM({})) > 0))";
    private final static String TOP = "AND({}>=LARGE({},{}),AND(NOT(ISBLANK({})), LEN(TRIM({})) > 0))";
    private final static String TOP_PERCENT = "{}>=ROUND(PERCENTILE({},{}),0)";
    private final static String BOTTOM_PERCENT = "{}<=ROUND(PERCENTILE({},{}),0)";


    @Override
    public void handle(XSSFSheetConditionalFormatting sheetCF, UStyle style, String operator, JSONObject uRule, CellRangeAddress[] ranges) {
        //是否为最后 true 最后 false 最前
        Boolean isBottom = uRule.getBool("isBottom");
        //是否为百分比 true为百分比 false不为百分比
        Boolean isPercent = uRule.getBool("isPercent");
        Integer value = uRule.getInt("value");

        String firstLocationName = getFirstLocationName(ranges);

        String finalLocation = getFinalLocation(ranges);
        String formula;
        if(isPercent){
            BigDecimal bigDecimal = new BigDecimal(value).divide(BigDecimal.valueOf(100),1, RoundingMode.HALF_UP);
            if(isBottom){
                formula = StrUtil.format(BOTTOM_PERCENT,firstLocationName,finalLocation,bigDecimal.doubleValue());
            }else{
                formula = StrUtil.format(TOP_PERCENT,firstLocationName,finalLocation,1-bigDecimal.doubleValue());
            }
        }else{
            if(isBottom){
                formula = StrUtil.format(BOTTOM,firstLocationName,finalLocation,value,firstLocationName,firstLocationName);
            }else{
                formula = StrUtil.format(TOP,firstLocationName,finalLocation,value,firstLocationName,firstLocationName);
            }
        }
        XSSFConditionalFormattingRule conditionalFormattingRule = sheetCF.createConditionalFormattingRule(formula);

        ExcelUtils.setXSSFPatternFormatting(conditionalFormattingRule,style);

        sheetCF.addConditionalFormatting(ranges,conditionalFormattingRule);
    }
}
