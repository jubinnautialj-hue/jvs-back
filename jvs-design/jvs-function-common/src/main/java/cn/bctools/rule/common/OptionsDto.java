package cn.bctools.rule.common;

import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
public class OptionsDto {

    /**
     * 说明
     */
    String name;
    /**
     * 字段名
     */
    String filed;

    InputType type;

}
