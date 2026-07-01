package cn.bctools.design.workflow.support.condition.impl;

import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.enums.DefaultParamEnums;
import cn.bctools.design.workflow.model.ConditionProperties;
import cn.bctools.design.workflow.model.condition.ConditionFun;
import cn.bctools.design.workflow.model.enums.ConditionPropertiesTypeEnum;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.condition.ConditionInterface;
import cn.bctools.function.handler.ExpressionHandler;
import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhuxiaokang
 * 公式条件判断
 */
@Slf4j
@Component
@AllArgsConstructor
public class ConditionFunImpl implements ConditionInterface {

    public static final String FLOW_EXPR_USE_CASE = "flowItemValue";
    private final ExpressionHandler handler;


    @Override
    public String getType() {
        return ConditionPropertiesTypeEnum.FUN.getValue();
    }

    @Override
    public Boolean verify(ConditionProperties conditionProperties, RuntimeData runtimeData) {
        ConditionFun conditionFun = JSON.parseArray(JSON.toJSONString(conditionProperties.getValues()), ConditionFun.class).get(0);
        Object result = handler.calculate(conditionFun.getExpr(), getData(runtimeData), FLOW_EXPR_USE_CASE);
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        log.warn("公式执行结果值不是Boolean类型, 默认表示不满足条件");
        return Boolean.FALSE;
    }

    /**
     * 获取数据
     *
     * @return 数据
     */
    private Map<String, Object> getData(RuntimeData runtimeData) {
        Map<String, Object> data = new HashMap<>(1);
        FlowTask flowTask = runtimeData.getFlowTask();
        // 获取工作流任务默认字段
        Map<String, Object> defaultParam = getFlowTaskDefaultParam(flowTask);
        if (MapUtils.isNotEmpty(defaultParam)) {
            data.putAll(defaultParam);
        }
        // 获取工作流任务数据模型字段
        Map<String, Object> dataParam = runtimeData.getData();
        if (MapUtils.isNotEmpty(dataParam)) {
            data.putAll(dataParam);
        }
        return data;
    }

    /**
     * 获取工作流任务默认字段
     *
     * @param flowTask 工作流任务信息
     * @return 工作流任务默认字段
     */
    private Map<String, Object> getFlowTaskDefaultParam(FlowTask flowTask) {
        Map<String, Object> map = new HashMap<>(DefaultParamEnums.values().length);
        for (DefaultParamEnums value : DefaultParamEnums.values()) {
            if (DefaultParamEnums.TASK_ID.equals(value)) {
                map.put(value.getId(), flowTask.getId());
            }
            if (DefaultParamEnums.CREATE_BY.equals(value)) {
                map.put(value.getId(), flowTask.getCreateBy());
            }
            if (DefaultParamEnums.CREATE_TIME.equals(value)) {
                map.put(value.getId(), flowTask.getCreateTime());
            }
        }
        return map;
    }

}
