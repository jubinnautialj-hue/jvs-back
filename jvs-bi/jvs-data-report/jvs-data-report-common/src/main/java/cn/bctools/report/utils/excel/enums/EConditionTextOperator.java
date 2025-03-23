package cn.bctools.report.utils.excel.enums;

import cn.bctools.report.utils.excel.highlight.HighlightCellHandler;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EConditionTextOperator {

    /*text*/
    containsText("ISNUMBER(SEARCH(\"{}\", A1))",true),
    notContainsText("NOT(ISNUMBER(SEARCH(\"{}\", A1)))",true),
    beginsWith("COUNTIF(A1, \"{}*\")",true),
    endsWith("COUNTIF(A1, \"*{}\")",true),
    equal("A1=\"{}\"",true),
    notEqual("A1<>\"{}\"",true),
    containsBlanks("ISBLANK(A1)",false),
    notContainsBlanks("NOT(ISBLANK(A1))",false),
    containsErrors("ISERROR(A1)",false),
    notContainsErrors("NOT(ISERROR(A1))",false),
    ;

    private final String format;
    private final Boolean hasParams;

    public String getFormatAndReplace(String locationName){
        if(StrUtil.isBlank(locationName) || StrUtil.equals(locationName, HighlightCellHandler.defaultLocationName)){
            return this.format;
        }
        return StrUtil.replace(this.format,HighlightCellHandler.defaultLocationName,locationName);
    }
}
