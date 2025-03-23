package cn.bctools.data.factory.doris.condition;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;

/**
 * 求和
 *
 * @author xiaohui
 */
@Component
public class SumDorisCollect implements DorisCollectCondition {


    @Override
    public String addCondition(String key, Integer decimalPlace, Boolean truncation, String asName) {
        String string = "SUM(`" + key + "`) ";
        if (StrUtil.isNotBlank(asName)) {
            string += "AS `" + asName + "`";
        }
        return string;
    }
}
