package cn.bctools.design.data.fields.enums;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.rule.dto.LinkTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class LinkTypeDto {

    /**
     * 类型 formula    value
     */
    LinkTypeEnum prop;
    /**
     * 字段名
     */
    String fieldKey;

    public String getFieldKey() {
        if (ObjectNull.isNull(this.fieldKey)) {
            return this.key;
        }
        return fieldKey;
    }

    /**
     * 支持兼容前端组件
     */
    String key;

    /**
     * 公式内容
     */
    String formulaContent;
    /**
     * 字段值
     */
    Object value;
    /**
     * 公式id
     */
    String formula;
    /**
     * 条件处理
     */
    DataQueryType enabledQueryTypes;

}
