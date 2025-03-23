package cn.bctools.data.factory.doris.condition;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;

/**
 * 计算
 *
 * @author xiaohui
 */
@Component
public class CountDorisCollect implements DorisCollectCondition {

    @Override
    public String addCondition(String key, Integer decimalPlace, Boolean truncation, String asName) {
        String string = "COUNT(`" + key + "`)";
        if (StrUtil.isNotBlank(asName)) {
            string += "AS `" + asName + "`";
        }
        return string;
    }


}
