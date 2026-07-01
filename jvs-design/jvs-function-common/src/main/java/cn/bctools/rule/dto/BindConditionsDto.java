package cn.bctools.rule.dto;

import cn.bctools.rule.entity.enums.DataQueryType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class BindConditionsDto {
    /**
     * 字段名
     */
    String fieldKey;
    /**
     * 公式类型
     */
    String formulaContent;
    /**
     * 类型
     */
    LinkTypeEnum prop;
    /**
     * 赋值
     */
    String value;
    /**
     * 匹配的条件类型
     */
    DataQueryType enabledQueryTypes;
}
