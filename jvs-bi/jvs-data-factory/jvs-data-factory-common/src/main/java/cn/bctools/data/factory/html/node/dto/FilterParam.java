package cn.bctools.data.factory.html.node.dto;

import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.enums.IntervalTypeEnum;
import cn.bctools.data.factory.query.value.enums.QueryEnums;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FilterParam {
    /**
     * 编号
     */
    private String relationshipNo;
    /**
     * 字段名称
     */
    private String fieldName;
    /**
     * 判断条件
     */
    private QueryEnums method;
    /**
     * 格式
     */
    private String format;
    /**
     * 字段key
     */
    private String fieldKey;
    /**
     * 比较值所有值都转为 字符串 如果是 对象就转为 jsonString
     */
    private String methodValue;
    /**
     * 数据id
     */
    private String dataId;
    /**
     * 字段类型
     */
    private DataFieldTypeEnum fieldType;

    private IntervalTypeEnum intervalType = IntervalTypeEnum.ClosedInterval;
}
