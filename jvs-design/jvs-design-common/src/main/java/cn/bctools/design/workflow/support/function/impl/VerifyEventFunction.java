package cn.bctools.design.workflow.support.function.impl;

import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.properties.AutomationProperties;
import cn.bctools.design.workflow.model.properties.FlowButton;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.function.AbstractFunctionHandler;
import cn.bctools.design.workflow.support.function.dto.RunRuleDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @author zhuxiaokang
 * 审批触发事件
 */
@Slf4j
@Component
@AllArgsConstructor
public class VerifyEventFunction extends AbstractFunctionHandler<Boolean, RuntimeData> {

    private final RuleRunFunction ruleRunFunction;

    /**
     * 是否触发审批事件
     *
     * @param node        节点
     * @param runtimeData 运行时数据
     * @return TRUE-触发，FALSE-未触发
     */
    @Override
    public Boolean invoke(Node node, RuntimeData runtimeData) {
        // 获取按钮配置
        List<FlowButton> btns = node.getProps().getBtn();
        if (CollectionUtils.isEmpty(btns)) {
            return Boolean.FALSE;
        }
        // 得到当前操作对应的按钮的事件（逻辑引擎）配置
        NodeOperationTypeEnum nodeOperationTypeEnum = runtimeData.getFlowDto().getNodeOperationType();
        Optional<FlowButton> optionalFlowButton = btns.stream().filter(btn -> btn.getOperation().equals(nodeOperationTypeEnum)).findFirst();
        if (!optionalFlowButton.isPresent()) {
            return Boolean.FALSE;
        }
        FlowButton flowButton = optionalFlowButton.get();
        AutomationProperties automationProperties = Optional.ofNullable(flowButton.getAutomation()).orElse(new AutomationProperties());
        String key = automationProperties.getKey();
        if (StringUtils.isBlank(key)) {
            return Boolean.FALSE;
        }
        RunRuleDto runRuleDto = new RunRuleDto()
                .setRuleKey(key)
                .setRuntimeData(runtimeData);
        ruleRunFunction.invoke(node, runRuleDto);
        return Boolean.TRUE;
    }
}
