package cn.bctools.design.rule.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class FunctionTestBody {
    Object value;
    String formulaContent;
}
