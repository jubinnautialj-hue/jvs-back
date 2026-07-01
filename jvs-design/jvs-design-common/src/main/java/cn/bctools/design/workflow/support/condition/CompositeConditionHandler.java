package cn.bctools.design.workflow.support.condition;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.design.workflow.model.Condition;
import cn.bctools.design.workflow.model.ConditionGroup;
import cn.bctools.design.workflow.model.ConditionProperties;
import cn.bctools.design.workflow.model.enums.ConditionOperatorEnum;
import cn.bctools.design.workflow.support.RuntimeData;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 条件判断入口
 */
@Component
public class CompositeConditionHandler {

    private final Map<String, ConditionInterface> conditionInterfaceMap;

    @Autowired
    public CompositeConditionHandler(List<ConditionInterface> conditionInterfaces) {
        this.conditionInterfaceMap = conditionInterfaces.stream().collect(Collectors.toMap(ConditionInterface::getType, Function.identity()));
    }

    /**
     * 条件校验
     *
     * @param condition 条件
     * @return TRUE-校验通过，FALSE-校验不通过
     */
    public Boolean verify(Condition condition, RuntimeData runtimeData) {
        // 条件组为空，直接返回校验不通过
        if (CollectionUtils.isEmpty(condition.getGroups())) {
            return Boolean.FALSE;
        }
        return verifyCondition(condition, runtimeData);
    }

    /**
     * 校验
     *
     * @param condition    条件
     * @param runtimeData  运行时数据
     * @return TRUE-校验通过，FALSE-校验不通过
     */
    private Boolean verifyCondition(Condition condition, RuntimeData runtimeData) {
        List<Boolean> verifys = new ArrayList<>();
        for (ConditionGroup group : condition.getGroups()) {
            if (CollectionUtils.isEmpty(group.getCondition())) {
                return Boolean.FALSE;
            }
            // 组内部条件判断结果集合
            List<Boolean> verifyList = new ArrayList<>();
            for (ConditionProperties conditionProperties : group.getCondition()) {
                ConditionInterface conditionInterface = Optional.ofNullable(conditionInterfaceMap.get(conditionProperties.getType().getValue())).orElseThrow(() -> new BusinessException("不支持的条件类型"));
                // 有条件节点未完成配置，直接返回校验不通过
                if (CollectionUtils.isEmpty(conditionProperties.getValues())) {
                    return Boolean.FALSE;
                }
                // 条件验证
                verifyList.add(conditionInterface.verify(conditionProperties, runtimeData));
            }
            // 满足的条件数量
            long passConditionNum = getPassConditionNum(verifyList);
            // 条件组内部条件“且运算”
            if (ConditionOperatorEnum.AND.equals(group.getConnection())) {
                verifys.add(passConditionNum == verifyList.size());
            }
            // 条件组内部条件“或运算”
            if (ConditionOperatorEnum.OR.equals(group.getConnection())) {
                verifys.add(passConditionNum >= 1);
            }
        }

        // 条件组关系运算
        long groupPassNum = getPassConditionNum(verifys);
        return (ConditionOperatorEnum.AND.equals(condition.getConnection()) && groupPassNum == verifys.size())
                ||
                (ConditionOperatorEnum.OR.equals(condition.getConnection()) && groupPassNum >=1);
    }

    /**
     * 获取满足的条件数量
     *
     * @param verifyList 条件验证结果集合
     * @return 满足的条件数量
     */
    private static long getPassConditionNum(List<Boolean> verifyList) {
       return verifyList.stream().filter(v -> v.equals(Boolean.TRUE)).count();
    }
}
