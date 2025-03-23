package cn.bctools.report.utils.excel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.poi.ss.usermodel.ComparisonOperator;

@Getter
@AllArgsConstructor
public enum EConditionNumberOperator {
    /*number*/
    between("AND(A1>{},A1<{})", ComparisonOperator.BETWEEN),
    notBetween("OR(A1<{},A1>{})",ComparisonOperator.NOT_BETWEEN),
    equal("A1={}",ComparisonOperator.EQUAL),
    notEqual("A1!={}",ComparisonOperator.NOT_EQUAL),
    greaterThan("A1>{}",ComparisonOperator.GT),
    greaterThanOrEqual("A1>={}",ComparisonOperator.GE),
    lessThan("A1<{}",ComparisonOperator.LT),
    lessThanOrEqual("A1<={}",ComparisonOperator.LE),


    ;

    private final String formula;
    private final byte operator;
}
