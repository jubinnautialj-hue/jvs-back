package cn.bctools.screen.chart.unit.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.screen.chart.unit.YoYMoMUnitSql;
import cn.bctools.screen.enums.YoYMoMUnitEnums;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * @author Administrator
 */
@Component
public class YoYMoMYearUnitSqlImpl implements YoYMoMUnitSql {
    private String DATE_MONTH_DIFF_YUAR_SQL = " CONCAT(  \n" +
            "    FLOOR(a.`{fieldKey}` / 100) - 1,\n" +
            "    LPAD(  \n" +
            "      a.`{fieldKey}` % 100, \n" +
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
                HashMap<Object, Object> hashMap = new HashMap<>();
                hashMap.put("fieldKey", pursuantKey);
                functionSql = StrUtil.format(DATE_MONTH_DIFF_YUAR_SQL, hashMap);
                break;
            case DATE_YEAR:
                functionSql = "a.`" + pursuantKey + "` - 1";
                break;
            default:
                throw new BusinessException("此类型字段无法做同环比");
        }
        return functionSql;
    }
}
