package cn.bctools.design.workflow.support.function.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.rule.RuleRunService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.service.FlowDynamicDataService;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.function.AbstractFunctionHandler;
import cn.bctools.design.workflow.support.function.dto.RunRuleDto;
import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author zhuxiaokang
 * 调用逻辑引擎
 */
@Slf4j
@Component
@AllArgsConstructor
public class RuleRunFunction extends AbstractFunctionHandler<Boolean, RunRuleDto> {

    private final FlowDynamicDataService flowDynamicDataService;
    private final RuleRunService ruleRunService;

    /**
     * 执行逻辑
     *
     * @param node       节点
     * @param runRuleDto 参数
     * @return TRUE-执行成功，FALSE-执行失败
     */
    @Override
    public Boolean invoke(Node node, RunRuleDto runRuleDto) {
        RuntimeData runtimeData = runRuleDto.getRuntimeData();
        FlowTask flowTask = runtimeData.getFlowTask();
        JSONObject dataMap = runtimeData.getData();

        try {
            // 先保存入参数据
            if (dataMap != null) {
                String dataId = dataMap.getString("dataId");
                if (ObjectNull.isNull(dataId)) {
                    flowDynamicDataService.onlySave(flowTask.getDataModelId(), dataMap);
                } else {
                    flowDynamicDataService.onlyUpdate(flowTask.getDataModelId(), dataId, dataMap);
                }
            }
            // 调用逻辑引擎
            ruleRunService.run(runRuleDto.getRuleKey(), dataMap);
            // 逻辑引擎可能修改了数据，所以要再查询一次得到最新的数据
            if (ObjectNull.isNotNull(flowTask.getDataModelId(), flowTask.getDataId())) {
                // 查询数据
                DynamicDataUtils.freePermit();
                dataMap = Convert.convert(JSONObject.class, flowDynamicDataService.querySingle(flowTask.getDataModelId(), flowTask.getDataId()));
            }
            runtimeData.setData(dataMap);
        } catch (Exception e) {
            throw new BusinessException("执行逻辑引擎失败", e.getMessage());
        }
        return Boolean.TRUE;
    }

}
