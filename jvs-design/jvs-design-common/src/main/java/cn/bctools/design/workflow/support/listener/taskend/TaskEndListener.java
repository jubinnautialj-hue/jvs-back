package cn.bctools.design.workflow.support.listener.taskend;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.rule.RuleRunService;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.dto.FlowExtendDto;
import cn.bctools.design.workflow.entity.dto.TaskEndTriggerEventDto;
import cn.bctools.design.workflow.entity.dto.TaskTriggerEventSetting;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.function.Function;

/**
 * @author zhuxiaokang
 * 流程结束监听
 */
@Slf4j
@Component
@AllArgsConstructor
public class TaskEndListener {

    private final RuleRunService ruleRunService;

    /**
     * 监听流程任务结束事件
     *
     * @param event 事件对象
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskEndEvent(TaskEndEvent event) {
       // 任务结束触发事件
        endTaskTriggerEvent(event.getFlowTask(), event.getExtend());
    }

    /**
     * 任务结束触发事件
     *
     * @param task   工作流任务
     * @param extend 工作流设计扩展配置
     */
    private void endTaskTriggerEvent(FlowTask task, FlowExtendDto extend) {
        TaskEndTriggerEventDto taskEndTriggerEvent = extend.getTaskEndTriggerEvent();
        // 未设置任务结束触发事件，不处理
        if (ObjectNull.isNull(taskEndTriggerEvent)) {
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
        switch (task.getTaskStatus()) {
            case PASSED:
                enableRuleKey = getKeyFunction.apply(taskEndTriggerEvent.getPassedEvent());
                break;
            case REJECTED:
                enableRuleKey = getKeyFunction.apply(taskEndTriggerEvent.getRejectedEvent());
                break;
            case TERMINATED:
                enableRuleKey = getKeyFunction.apply(taskEndTriggerEvent.getTerminatedEvent());
                break;
            default:
                break;
        }

        // 触发逻辑
        if (ObjectNull.isNull(enableRuleKey)) {
            return;
        }
        // 封装传递到逻辑引擎的参数
        // 这里只传递基础数据, 若逻辑引擎需要任务的其它数据，在逻辑中查询数据即可
        JSONObject data = new JSONObject();
        data.put("id", task.getDataId());
        data.put("dataId", task.getDataId());
        data.put("modelId", task.getDataModelId());
        try {
            // 调用逻辑引擎
            ruleRunService.run(enableRuleKey, data);
        } catch (Exception e) {
            throw new BusinessException("任务结束触发事件——执行逻辑引擎失败", e);
        }
    }
}
