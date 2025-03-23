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

@Component
@Slf4j
public class AverageHandler implements HighlightCellHandler{

    private static final String greaterThan = "greaterThan";
    private static final String lessThan = "lessThan";
    private static final String GREATER_THAN_FORMAT = "{}>=AVERAGE({})";
    private static final String LESS_THAN_FORMAT = "{}<=AVERAGE({})";

    @Override
    public void handle(XSSFSheetConditionalFormatting sheetCF, UStyle style, String operator, JSONObject uRule, CellRangeAddress[] ranges) {
        String finalLocation = getFinalLocation(ranges);
        String firstLocationName = getFirstLocationName(ranges);
        String formula;
        if(greaterThan.equals(operator)){
            formula = StrUtil.format(GREATER_THAN_FORMAT, firstLocationName, finalLocation);
        }else{
            formula = StrUtil.format(LESS_THAN_FORMAT, firstLocationName, finalLocation);
        }
        XSSFConditionalFormattingRule conditionalFormattingRule = sheetCF.createConditionalFormattingRule(formula);
        ExcelUtils.setXSSFPatternFormatting(conditionalFormattingRule,style);
        sheetCF.addConditionalFormatting(ranges, conditionalFormattingRule);
    }
}
