package cn.bctools.report.utils.excel.highlight;

import cn.bctools.report.model.univer.style.UStyle;
import cn.bctools.report.utils.ExcelUtils;
import cn.bctools.report.utils.excel.enums.EConditionNumberOperator;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFSheetConditionalFormatting;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NumberHandler implements HighlightCellHandler{

    @Override
    public void handle(XSSFSheetConditionalFormatting sheetCF, UStyle style, String operator, JSONObject uRule, CellRangeAddress[] ranges) {
        EConditionNumberOperator conditionOperator = EConditionNumberOperator.valueOf(operator);
        XSSFConditionalFormattingRule conditionalFormattingRule;
        if(EConditionNumberOperator.between.equals(conditionOperator) || EConditionNumberOperator.notBetween.equals(conditionOperator)){
            JSONArray jsonArray = uRule.getJSONArray("value");
            conditionalFormattingRule = sheetCF.createConditionalFormattingRule(conditionOperator.getOperator(),jsonArray.getStr(0),jsonArray.getStr(1));
        }else{
            String value = uRule.getStr("value");
            conditionalFormattingRule = sheetCF.createConditionalFormattingRule(conditionOperator.getOperator(),value);
        }

        ExcelUtils.setXSSFPatternFormatting(conditionalFormattingRule,style);
        sheetCF.addConditionalFormatting(ranges,conditionalFormattingRule);
    }
}
