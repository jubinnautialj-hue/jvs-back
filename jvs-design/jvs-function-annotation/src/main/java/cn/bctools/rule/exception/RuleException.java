package cn.bctools.rule.exception;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.rule.entity.enums.RuleExceptionEnum;

/**
 * 逻辑引擎的执行错误
 * 用于逻辑执行过程中获取公式时，获取变量的值可能会失败
 */
public class RuleException extends BusinessException {

    private static final long serialVersionUID = -1;

    public RuleException(RuleExceptionEnum rule) {
        super(rule.name(), rule.getCode());
    }

    /**
     * 自定义 message
     *
     * @param rule
     * @param msg
     */
    public RuleException(RuleExceptionEnum rule, String msg) {
        super(msg, rule.getCode());
    }


}
