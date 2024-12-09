package cn.bctools.design.workflow.support.node;

import cn.bctools.design.workflow.dto.FlowReqDto;
import cn.bctools.design.workflow.enums.FlowDataFieldEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.model.properties.AutomationProperties;
import cn.bctools.design.workflow.support.impl.AbstractFlowHandler;
import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.enums.FlowNextTypeEnum;
import cn.bctools.design.workflow.support.function.dto.RunRuleDto;
import cn.bctools.design.workflow.support.function.impl.RuleRunFunction;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author zhuxiaokang
 * 逻辑节点处理
 */
@Slf4j
@Component
@AllArgsConstructor
public class AutomationFlowHandler extends AbstractFlowHandler {

    private final RuleRunFunction ruleRunFunction;

    /**
     * 逻辑引擎调用工作流时，需要在FlowDto.data中加入此参数。否则工作流不继续流转
     */
    public static final String KEY_PARAM = FlowDataFieldEnum.RULE_KEY.getFieldKey();

    @Override
    public NodeTypeEnum getType() {
        return NodeTypeEnum.AUTOMATION;
    }

    @Override
    protected void handle(Node node, RuntimeData runtimeData) {
        AutomationProperties automationProperties = Optional.ofNullable(node.getProps().getAutomation()).orElse(new AutomationProperties());
        String key = automationProperties.getKey();
        // 未配置逻辑引擎,表示需要等待被调用
        FlowResult flowResult = null;
        if (StringUtils.isBlank(key)) {
            flowResult = notConfiguredKeyProcess(node, runtimeData);
        } else {
            // 已配置逻辑引擎处理
            flowResult= keyProcess(key, node, runtimeData);
        }
        FlowContextUtil.refreshContext(flowResult);
    }

    /**
     * 未配置逻辑引擎处理
     *
     * @param node        当前节点
     * @param runtimeData 运行时数据
     * @return 流转结果
     */
    private FlowResult notConfiguredKeyProcess(Node node, RuntimeData runtimeData) {
        JSONObject data = Optional.ofNullable(runtimeData.getFlowDto()).map(FlowReqDto::getData).orElse(new JSONObject());
        String key = data.getString(KEY_PARAM);
        FlowResult flowResult = new FlowResult();
        if (StringUtils.isBlank(key)) {
            // 未配置逻辑引擎，且执行”集成&自动化“节点时，未传递逻辑引擎key，则挂起当前节点，等待调用
            flowResult.setFlowNextTypeEnum(FlowNextTypeEnum.HANG);
        } else {
            // 未配置逻辑引擎，且执行”集成&自动化“节点时，已传递逻辑引擎key，则完成当前节点,设置下一个节点
            flowResult.setNextNode(node);
        }
        return flowResult;
    }

    /**
     * 已配置逻辑引擎处理
     *
     * @param key         逻辑引擎key
     * @param node        当前节点
     * @param runtimeData 运行时数据
     * @return 流转结果
     */
    private FlowResult keyProcess(String key, Node node, RuntimeData runtimeData) {
        RunRuleDto runRuleDto = new RunRuleDto()
                .setRuleKey(key)
                .setRuntimeData(runtimeData);
        ruleRunFunction.invoke(node, runRuleDto);
        return new FlowResult().setNextNode(node);
    }
}
