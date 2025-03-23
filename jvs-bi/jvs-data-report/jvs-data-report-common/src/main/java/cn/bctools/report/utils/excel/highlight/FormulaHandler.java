package cn.bctools.report.utils.excel.highlight;

import cn.bctools.report.model.univer.style.UStyle;
import cn.bctools.report.utils.ExcelUtils;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFSheetConditionalFormatting;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FormulaHandler implements HighlightCellHandler{

    @Override
    public void handle(XSSFSheetConditionalFormatting sheetCF, UStyle style, String operator, JSONObject uRule, CellRangeAddress[] ranges) {
        String formula = uRule.getStr("value");
        XSSFConditionalFormattingRule conditionalFormattingRule = sheetCF.createConditionalFormattingRule(formula);
        ExcelUtils.setXSSFPatternFormatting(conditionalFormattingRule,style);
        sheetCF.addConditionalFormatting(ranges, conditionalFormattingRule);
    }
}
