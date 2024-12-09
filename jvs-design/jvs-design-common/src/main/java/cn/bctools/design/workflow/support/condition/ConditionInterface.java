package cn.bctools.design.workflow.support.condition;


import cn.bctools.design.workflow.model.ConditionProperties;
import cn.bctools.design.workflow.support.RuntimeData;

/**
 * @author zhuxiaokang
 * 工作流支持的各种条件实现接口
 */
public interface ConditionInterface {

    /**
     * 条件类型
     *
     * @return 条件类型
     */
    String getType();

    /**
     * 条件验证
     *
     * @param conditionProperties 条件
     * @param runtimeData         运行时数据
     * @return TRUE-满足条件，FALSE-不满足条件
     */
    Boolean verify(ConditionProperties conditionProperties, RuntimeData runtimeData);
}
