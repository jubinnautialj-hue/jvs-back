package cn.bctools.data.factory.doris.condition;

import cn.bctools.data.factory.dto.DataSourceField;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import org.springframework.stereotype.Component;

/**
 * 添加最大值的条件
 *
 * @author xiaohui
 */
@Component
public class MaxDorisCollect implements DorisCollectCondition {

    @Override
    public DataSourceField fieldGenerate(DataSourceField factoryDataSourceField, Integer decimalPlace) {
        //如果原来的是时间就为时间 其他的就是数字
        return factoryDataSourceField.setFieldKey(SecureUtil.md5(factoryDataSourceField.getFieldName()));
    }


    @Override
    public String addCondition(String key, Integer decimalPlace, Boolean truncation, String asName) {
        String string = "MAX(`" + key + "`) ";
        if (StrUtil.isNotBlank(asName)) {
            string += "AS `" + asName + "`";
        }
        return string;
    }
}
