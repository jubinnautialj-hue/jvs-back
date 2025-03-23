package cn.bctools.data.factory.query;

import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.enums.IntervalTypeEnum;
import cn.bctools.data.factory.query.value.enums.QueryEnums;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class QueryExecDto {
    /**
     * 编号--与表达式编号一致
     */
    private String relationshipNo;
    /**
     * 格式化
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
     * 是否为动态值  例如 近3个月  一个月  当前登录用户  当前登录用户部门  角色  等等
     */
    private Boolean isDynamic;
    /**
     * 字段类型
     */
    private DataFieldTypeEnum fieldType;
    /**
     * 是否为 用户拓展字段
     */
    private Boolean isUserExtension;
    /**
     * 区间类型
     */
    private IntervalTypeEnum intervalType;
    /**
     * 判断条件
     */
    private QueryEnums method;
}
