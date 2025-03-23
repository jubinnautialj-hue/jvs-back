package cn.bctools.data.factory.html.node.dto;


import cn.bctools.data.factory.enums.IntervalTypeEnum;
import cn.bctools.data.factory.query.value.enums.QueryEnums;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ConditionGroupNodeDto {
    /**
     * 判断条件
     */
    private QueryEnums method;
    /**
     * 比较值所有值都转为 字符串 如果是 对象就转为 jsonString
     */
    private String methodValue;

    private IntervalTypeEnum intervalType = IntervalTypeEnum.ClosedInterval;
}
