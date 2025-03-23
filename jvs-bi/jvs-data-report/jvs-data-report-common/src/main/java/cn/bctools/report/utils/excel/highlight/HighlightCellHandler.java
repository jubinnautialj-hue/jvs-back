package cn.bctools.report.utils.excel.highlight;

import cn.bctools.report.model.univer.style.UStyle;
import cn.hutool.json.JSONObject;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheetConditionalFormatting;

import java.util.Arrays;
import java.util.Comparator;

public interface HighlightCellHandler {

    String defaultLocationName = "A1";

    void handle(XSSFSheetConditionalFormatting sheetCF, UStyle style, String operator, JSONObject uRule, CellRangeAddress[] ranges);

    default String getFirstLocationName(CellRangeAddress[] ranges){
        if(ranges == null || ranges.length == 0){
            return defaultLocationName;
        }
        CellRangeAddress range = ranges[0];
        return getFirstLocationName(range);
    }

    default String getFirstLocationName(CellRangeAddress range){
        int firstColumn = range.getFirstColumn();
        int firstRow = range.getFirstRow()+1;
        String colName = ExcelUtil.indexToColName(firstColumn);
        return colName+firstRow;
    }

    default String getFinalLocation(CellRangeAddress range){
        int firstColumn = range.getFirstColumn();
        int firstRow = range.getFirstRow()+1;
        String colName = ExcelUtil.indexToColName(firstColumn);
        String start = StringPool.DOLLAR+colName+StringPool.DOLLAR+firstRow;

        int lastColumn = range.getLastColumn();
        int lastRow= range.getLastRow()+1;
        String lastColName = ExcelUtil.indexToColName(lastColumn);
        String end = StringPool.DOLLAR+lastColName+StringPool.DOLLAR+lastRow;
        return start+StringPool.COLON+end;
    }

    default String getFinalLocation(CellRangeAddress[] ranges){
        if (ranges.length>1) {
            Comparator<CellRangeAddress> fistComparator = (o1, o2) -> {
                if(o1.getFirstRow()<o2.getFirstRow()){
                    return -1;
                }
                if(o1.getFirstRow()>o2.getFirstRow()){
                    return 1;
                }
                return Integer.compare(o1.getFirstColumn(), o2.getFirstColumn());
            };
            Comparator<CellRangeAddress> lastComparator = (o1, o2) -> {
                if(o1.getLastRow()>o2.getLastRow()){
                    return 1;
                }
                if(o1.getLastRow()<o2.getLastRow()){
                    return -1;
                }
                return Integer.compare(o1.getLastColumn(), o2.getLastColumn());
            };

            CellRangeAddress first = Arrays.stream(ranges).min(fistComparator).get();
            CellRangeAddress last = Arrays.stream(ranges).max(lastComparator).get();
            CellRangeAddress cellAddresses = new CellRangeAddress(first.getFirstRow(), last.getLastRow(), first.getFirstColumn(), last.getLastColumn());
            return getFinalLocation(cellAddresses);
        }else{
            return getFinalLocation(ranges[0]);
        }
    }

}
