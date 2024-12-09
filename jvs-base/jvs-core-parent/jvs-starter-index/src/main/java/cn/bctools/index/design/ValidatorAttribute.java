package cn.bctools.index.design;

import cn.bctools.index.design.enums.ValidatorType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jvs The type Validator attribute.
 */
@Data
@Accessors(chain = true)
public class ValidatorAttribute {

    /**
     * 校验类型
     */
    private ValidatorType type;

    /**
     * 提示
     */
    private String message;
}
