package cn.bctools.data.factory.doris.condition;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;

/**
 * 平均
 *
 * @author xiaohui
 */
@Component
public class AvgDorisCollect implements DorisCollectCondition {

    @Override
    public String addCondition(String key, Integer decimalPlace, Boolean truncation, String asName) {
        String collectSql;
        if (truncation) {
            collectSql = "truncate( ";
        } else {
            collectSql = "round( ";
        }
        String string = collectSql + "AVG(`" + key + "`) ," + decimalPlace + ")";
        if (StrUtil.isNotBlank(asName)) {
            string += " AS `" + asName + "`";
        }
        return string;
    }
}
