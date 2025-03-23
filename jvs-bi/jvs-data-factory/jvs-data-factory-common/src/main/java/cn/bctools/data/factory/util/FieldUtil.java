package cn.bctools.data.factory.util;


import cn.bctools.data.factory.dto.DataSourceField;
import cn.hutool.crypto.SecureUtil;

import java.util.List;

/**
 * 字段生成规则
 *
 * @author xiaohui
 */
public class FieldUtil {

    /**
     * 执行过程中生成的数据字段名称同一生成规则
     *
     * @param fieldName 需要重新命名的名称
     * @param list      需要对比的所有字段
     * @param isCover   是否为覆盖
     */
    public static String getFieldName(String fieldName, List<DataSourceField> list, Boolean isCover) {
        String name = SecureUtil.md5(fieldName);
        if (isCover) {
            return name;
        }
        String finalName = name;
        long count = list.stream().filter(e -> e.getFieldKey().contains(finalName)).count();
        if (count > 0) {
            count++;
            name = name + count;
        }
        return name;
    }
}
