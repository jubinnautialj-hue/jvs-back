package cn.bctools.data.factory.doris.condition;

import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.hutool.crypto.SecureUtil;

/**
 * @author xiaohui
 * <p>
 * 分组汇总 汇总的条件添加
 */
public interface DorisCollectCondition {
    /**
     * 添加查询条件
     *
     * @param key          字段名称
     * @param decimalPlace 小数位数
     * @param truncation   是否截断
     * @param asName       别名 别名如果为空 就不会使用 as 生成别名
     * @return sql
     */
    String addCondition(String key, Integer decimalPlace, Boolean truncation, String asName);

    /**
     * 字段生成 不同的计算方式与不同类型的字段返回值 不一样
     *
     * @param factoryDataSourceField 最新的结构
     * @param decimalPlace           小数位数
     * @return 分组对象
     */
    default DataSourceField fieldGenerate(DataSourceField factoryDataSourceField, Integer decimalPlace) {
        //只有数字字段才能计算平均值 所以返回值就是 DECIMAL(%s,%s)
        return new DataSourceField()
                .setDorisType("DECIMAL(%s,%s)")
                .setDataId(factoryDataSourceField.getDataId())
                .setLength(18)
                .setPrecision(decimalPlace)
                .setFieldKey(SecureUtil.md5(factoryDataSourceField.getFieldName()))
                .setFieldName(factoryDataSourceField.getFieldName())
                .setIsShow(Boolean.TRUE)
                .setFieldType(DataFieldTypeEnum.DECIMAL)
                .setDataFieldTypeClassify(DataFieldTypeClassifyEnum.数字);
    }
}
