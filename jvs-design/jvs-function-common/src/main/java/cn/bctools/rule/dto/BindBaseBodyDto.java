package cn.bctools.rule.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class BindBaseBodyDto {

    /**
     * 类型 formula    value
     */
    LinkTypeEnum prop;
    /**
     * 字段名
     */
    String fieldKey;
    /**
     * 字段动态的 key 用于选择绑定的属性字段值。
     */
    String fieldDynamicKey;
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
}
