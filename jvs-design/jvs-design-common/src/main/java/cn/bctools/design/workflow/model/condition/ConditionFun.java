package cn.bctools.design.workflow.model.condition;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhuxiaokang
 *条件类型为“公式”的值对象
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class ConditionFun extends ConditionValueBase {

    /**
     * 表达式
     */
    private String expr;
}
