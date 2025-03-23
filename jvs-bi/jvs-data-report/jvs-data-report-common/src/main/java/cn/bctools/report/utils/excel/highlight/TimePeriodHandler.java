package cn.bctools.report.utils.excel.highlight;

import cn.bctools.report.model.univer.style.UStyle;
import cn.bctools.report.utils.ExcelUtils;
import cn.bctools.report.utils.excel.enums.EConditionTimePeriodOperator;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFSheetConditionalFormatting;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TimePeriodHandler implements HighlightCellHandler{

    @Override
    public void handle(XSSFSheetConditionalFormatting sheetCF, UStyle style, String operator, JSONObject uRule, CellRangeAddress[] ranges) {
        String locationName = getFirstLocationName(ranges);
        EConditionTimePeriodOperator conditionOperator = EConditionTimePeriodOperator.valueOf(operator);
        XSSFConditionalFormattingRule conditionalFormattingRule;
        String formula = conditionOperator.getFormatAndReplace(locationName);
        conditionalFormattingRule = sheetCF.createConditionalFormattingRule(formula);
        ExcelUtils.setXSSFPatternFormatting(conditionalFormattingRule,style);
        sheetCF.addConditionalFormatting(ranges,conditionalFormattingRule);
    }
}
