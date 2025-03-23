package cn.bctools.data.factory.doris.condition;

import cn.bctools.data.factory.dto.DataSourceField;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;

/**
 * 最小值
 *
 * @author xiaohui
 */
@Component
public class MinDorisCollect implements DorisCollectCondition {

    @Override
    public DataSourceField fieldGenerate(DataSourceField factoryDataSourceField, Integer decimalPlace) {
        //如果原来的是时间就为时间 其他的就是数字
        return factoryDataSourceField;
    }


    @Override
    public String addCondition(String key, Integer decimalPlace, Boolean truncation, String asName) {
        String string = "MIN(`" + key + "`) ";
        if (StrUtil.isNotBlank(asName)) {
            string += "AS `" + asName + "`";
        }
        return string;
    }
}
