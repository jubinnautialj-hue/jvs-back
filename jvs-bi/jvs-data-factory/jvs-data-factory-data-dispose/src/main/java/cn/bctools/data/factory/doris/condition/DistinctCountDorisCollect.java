package cn.bctools.data.factory.doris.condition;

import cn.bctools.common.utils.IdGenerator;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;

/**
 * 去重
 *
 * @author xiaohui
 */
@Component
public class DistinctCountDorisCollect implements DorisCollectCondition {



    @Override
    public String addCondition(String key, Integer decimalPlace, Boolean truncation, String asName) {
        String string = "COUNT( DISTINCT `" + key + "`)";
        if (StrUtil.isNotBlank(asName)) {
            string += "AS `" + asName + "`";
        }
        return string;
    }
}
