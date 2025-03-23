package cn.bctools.chart.chart.compare.unit.impl;

import cn.bctools.chart.chart.compare.unit.YoYMoMUnitSql;
import cn.bctools.chart.enums.YoYMoMUnitEnums;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * @author Administrator
 */
@Component
public class YoYMoMMonthUnitSqlImpl implements YoYMoMUnitSql {

    private String DATE_MONTH_DIFF_MONTH_SQL = " CONCAT(  " +
            "    CASE  " +
            "      WHEN (a.`{fieldKey}` % 100) = 1 THEN FLOOR(a.`{fieldKey}` / 100)-1 " +
            "      ELSE FLOOR(a.`{fieldKey}` / 100)  \n" +
            "    END,  \n" +
            "    LPAD(  \n" +
            "      CASE  \n" +
            "        WHEN (a.`{fieldKey}` % 100) = 1 THEN 12 \n" +
            "        ELSE (a.`{fieldKey}` % 100) - 1  \n" +
            "      END,  \n" +
            "      2, '0'  \n" +
            "    )  \n" +
            "  )";

    @Override
    public String functionSql(DataFieldTypeEnum dataFieldTypeEnum, Object value, String pursuantKey, YoYMoMUnitEnums yoYMoMUnitEnums) {
        String functionSql;
        switch (dataFieldTypeEnum) {
            case DATE:
                functionSql = YoYMoMUnitSql.super.functionSql(dataFieldTypeEnum, value, pursuantKey, yoYMoMUnitEnums);
                break;
            case DATE_YEAR_MONTH:
                HashMap<Object, Object> hashMap = new HashMap<>(1);
                hashMap.put("fieldKey", pursuantKey);
                functionSql = StrUtil.format(DATE_MONTH_DIFF_MONTH_SQL, hashMap);
                break;
            case DATE_MONTH:
                functionSql = "a.`" + pursuantKey + "` - 1";
                break;
            default:
                throw new BusinessException("此类型字段无法做同环比");

        }
        return functionSql;
    }
}
