package cn.bctools.design.workflow.support.function.impl;

import cn.bctools.design.workflow.model.Condition;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.condition.CompositeConditionHandler;
import cn.bctools.design.workflow.support.function.AbstractFunctionHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author zhuxiaokang
 * 条件处理
 */
@Slf4j
@Component
@AllArgsConstructor
public class ConditionFunction extends AbstractFunctionHandler<Condition, RuntimeData> {

    private final CompositeConditionHandler compositeConditionHandler;

    @Override
    public Condition invoke(Node node, RuntimeData runtimeData) {
        // 按条件分支优先级顺序校验（当前优先级规则：数组顺序）
        for (Condition condition : node.getConditions()) {
            // 默认条件分支，直接返回校验通过
            if (Boolean.TRUE.equals(condition.getDefaultCondition())) {
                return condition;
            }
            // 非默认条件分支，则校验条件
            if (Boolean.TRUE.equals(compositeConditionHandler.verify(condition, runtimeData))) {
                log.debug("满足条件分支：{}", condition.getName());
                return condition;
            }
        }
        // 所有条件分支都不满足
        return null;
    }
}
