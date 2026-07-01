package cn.bctools.design.workflow.support.listener.taskstart;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.rule.RuleRunService;
import cn.bctools.design.workflow.entity.dto.FlowExtendDto;
import cn.bctools.design.workflow.entity.dto.TaskStartTriggerEventDto;
import cn.bctools.design.workflow.entity.dto.TaskTriggerEventSetting;
import cn.bctools.rule.utils.html.RuleExecuteDto;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * @author jvs
 * 流程启动监听
 */
@Slf4j
@Component
@AllArgsConstructor
public class TaskStartListener {

    private final RuleRunService ruleRunService;

    /**
     * 执行流程启动逻辑
     *
     * @param event  逻辑类型
     * @param data   数据
     * @param extend 高级配置
     */
    public void onTaskStartEvent(TastStartEventEnum event, JSONObject data, FlowExtendDto extend) {
        // 任务结束触发事件
        startTaskTriggerEvent(event, data, extend);
    }

    /**
     * 任务结束触发事件
     *
     * @param event  工作流任务
     * @param data   数据
     * @param extend 工作流设计扩展配置
     */
    private void startTaskTriggerEvent(TastStartEventEnum event, JSONObject data, FlowExtendDto extend) {
        TaskStartTriggerEventDto taskStartTriggerEvent = extend.getTaskStartTriggerEvent();
        // 未设置任务结束触发事件，不处理
        if (ObjectNull.isNull(taskStartTriggerEvent)) {
            return;
        }

        // 得到启用的逻辑引擎key
        String enableRuleKey = null;
        Function<TaskTriggerEventSetting, String> getKeyFunction = setting -> {
            if (ObjectNull.isNull(setting)) {
                return null;
            }
            if (setting.getEnableEvent()) {
                return setting.getRuleKey();
            }
            return null;
        };

        if (TastStartEventEnum.BEFORE_START.equals(event)) {
            enableRuleKey = getKeyFunction.apply(taskStartTriggerEvent.getBeforeStartFlowEvent());
        }

        // 触发逻辑
        if (ObjectNull.isNull(enableRuleKey)) {
            return;
        }
        try {
            // 调用逻辑引擎
            RuleExecuteDto dto = ruleRunService.run(enableRuleKey, data);
            if (!dto.getStats()) {
                throw new BusinessException(dto.getSyncMessageTips());
            }
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }
}
