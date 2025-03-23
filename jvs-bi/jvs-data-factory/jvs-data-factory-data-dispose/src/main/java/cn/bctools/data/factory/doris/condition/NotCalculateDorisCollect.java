package cn.bctools.data.factory.doris.condition;

import cn.bctools.data.factory.dto.DataSourceField;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;

/**
 * 不聚合直接返回一个数组
 *
 * @author xiaohui
 */
@Component
public class NotCalculateDorisCollect implements DorisCollectCondition {


    @Override
    public String addCondition(String key, Integer decimalPlace, Boolean truncation, String asName) {
        String string = "ARRAY_AGG(`" + key + "`) ";
        if (StrUtil.isNotBlank(asName)) {
            string += "AS `" + asName + "`";
        }
        return string;
    }

    @Override
    public DataSourceField fieldGenerate(DataSourceField factoryDataSourceField, Integer decimalPlace) {
        return factoryDataSourceField;
    }
}
