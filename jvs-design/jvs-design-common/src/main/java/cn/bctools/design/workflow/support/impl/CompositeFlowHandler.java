package cn.bctools.design.workflow.support.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.common.OrderFormat;
import cn.bctools.design.data.entity.DataIdPo;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.service.DataIdService;
import cn.bctools.design.notice.handler.enums.TriggerTypeEnum;
import cn.bctools.design.notice.handler.util.NoticeVariableUtils;
import cn.bctools.design.util.OrderUtils;
import cn.bctools.design.workflow.constant.SystemConstant;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.dto.FlowExtendDto;
import cn.bctools.design.workflow.entity.dto.TaskCodeFormatDto;
import cn.bctools.design.workflow.entity.enums.FlowTaskStatusEnum;
import cn.bctools.design.workflow.entity.enums.TaskCodeFormatTypeEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.service.*;
import cn.bctools.design.workflow.support.FlowInterface;
import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.context.FlowContext;
import cn.bctools.design.workflow.support.function.dto.AutoApprovalDto;
import cn.bctools.design.workflow.support.function.impl.AutoApprovalFunction;
import cn.bctools.design.workflow.support.listener.asynctask.AsyncTaskDynamicDataEvent;
import cn.bctools.design.workflow.support.listener.notify.FlowNotifyEvent;
import cn.bctools.design.workflow.support.process.CompositeProcessHandler;
import cn.bctools.design.workflow.support.process.ProcessResult;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import cn.bctools.design.workflow.utils.FlowTaskNodeUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author zhuxiaokang
 * 工作流流转处理入口
 */
@Slf4j
@Component
@AllArgsConstructor
public class CompositeFlowHandler extends AbstractCompositeFlowHandler {

    private final List<FlowInterface> flowInterfaces;
    private final CompositeProcessHandler compositeProcessHandler;
    private final AutoApprovalFunction autoApprovalFunction;
    private final FlowTaskService flowTaskService;
    private final FlowTaskNodeService flowTaskNodeService;
    private final FlowTaskPathService flowTaskPathService;
    private final FlowTaskParallelService flowTaskParallelService;
    private final FlowTaskPersonService flowTaskPersonService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DataIdService dataIdService;
    private final FlowDynamicDataService flowDynamicDataService;

    @Override
    protected void processNode(Node currentNode, RuntimeData runtimeData) {
        // 节点逻辑处理
        flowInterfaces.stream()
                .filter(f -> f.getType().equals(currentNode.getType()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("节点类型暂未支持"))
                .execute();
    }

    @Override
    protected void flow(FlowResult flowResult) {
        compositeProcessHandler.execute(flowResult);
    }

    @Override
    protected void autoApproval() {
        FlowContext flowContext = FlowContextUtil.context().getContext();
        AutoApprovalDto autoApprovalDto = autoApprovalFunction.invoke(flowContext.getRuntimeData().getCurrentNode(), flowContext.getFlowResult());
        if (Boolean.FALSE.equals(autoApprovalDto.getEnable())) {
            return;
        }
        autoApprovalDto.getAutoTasks().forEach(autoTask -> {
            execute(autoTask.getCurrentNode(), autoTask);
        });

    }

    @Override
    protected void processFlowState() {
        FlowContext flowContext = FlowContextUtil.context().getContext();
        ProcessResult processResult = flowContext.getProcessResult();
        RuntimeData runtimeData = flowContext.getRuntimeData();
        switch (processResult.getProcessType()) {
            case NEXT:
                execute(processResult.getNextNode(), runtimeData);
                break;
            case END:
            default:
                // 不做处理
                break;
        }
    }

    @Override
    protected void after() {
        // 流转结果
        executeResult();
        // 发通知
        sendNotify();
    }

    /**
     * 流转结果
     */
    private void executeResult() {
        FlowContext flowContext = FlowContextUtil.context().getContext();
        RuntimeData runtimeData = flowContext.getRuntimeData();
        FlowTask flowTask = runtimeData.getFlowTask();
        // 启动流程
        if (flowContext.getStart()) {
            // 流程未结束
            if (FlowTaskStatusEnum.PENDING.equals(flowTask.getTaskStatus())) {
                // 保存流程可执行路径
                flowTaskPathService.saveBatch(runtimeData.getTaskPaths());
                // 修改任务节点
                if (ObjectNull.isNotNull(runtimeData.getFlowTaskNodes())) {
                    flowTaskNodeService.updateBatchById(runtimeData.getFlowTaskNodes());
                }
            } else {
                // 流程已结束
                if (ObjectNull.isNotNull(runtimeData.getFlowTaskNodes())) {
                    // 删除工作流流转节点数据
                    flowTaskNodeService.removeTaskAll(flowTask.getId());
                    // 删除待办人所有信息
                    flowTaskPersonService.removeTaskAll(flowTask.getId());
                }
                // 删除并行任务信息
                if (Boolean.TRUE.equals(flowContext.getExistsParallel())) {
                    flowTaskParallelService.removeTaskAll(flowTask.getId());
                }
            }

            // 保存工作流任务
            // 生成流程任务编号
            flowTask.setTaskCode(generateTaskCode(flowTask.getFlowDesignId(), runtimeData.getFlowExtend(), runtimeData.getTime()));
            // 自定义流程标题
            flowTask.setTitle(generateTaskTitle(flowTask, runtimeData.getData(), runtimeData.getFlowExtend()));
            flowTaskService.save(flowTask);
        } else {
            // 修改工作流任务
            flowTaskService.updateById(flowTask);
        }
        // 发布事件：同步流程信息到业务数据
        applicationEventPublisher.publishEvent(new AsyncTaskDynamicDataEvent(this, flowTask));
    }

    /**
     * 生成流程任务编号
     *
     * @param flowDesignId 流程设计id
     * @param flowExtend   流程高级配置
     * @param time         启动流程时间
     * @return 任务编号
     */
    private String generateTaskCode(String flowDesignId, FlowExtendDto flowExtend, String time) {
        TaskCodeFormatDto codeFormat = Optional.ofNullable(flowExtend).map(FlowExtendDto::getCodeFormat).orElseGet(TaskCodeFormatDto::new);
        // 未启用自定义流程编号格式，则默认生成自增序号
        if (Boolean.FALSE.equals(codeFormat.getCustom())) {
            codeFormat.setFormatType(TaskCodeFormatTypeEnum.AUTO);
            codeFormat.setAutoFormat(new OrderFormat().setOrderDigit(1));
        }
        // 自动计算生成流程编号
        DataIdPo nextId = dataIdService.nextId(DesignType.workflow, flowDesignId, 1);
        if (TaskCodeFormatTypeEnum.AUTO.equals(codeFormat.getFormatType())) {
            return OrderUtils.getOrderNumber(codeFormat.getAutoFormat(), nextId, DateUtil.parse(time));
        }
        return "";
    }

    /**
     * 根据流程标题格式配置生成title
     *
     * @param flowTask   流程任务
     * @param data       数据
     * @param flowExtend 流程扩展配置
     * @return 流程标题
     */
    private String generateTaskTitle(FlowTask flowTask, JSONObject data, FlowExtendDto flowExtend) {
        Map<String, Object> echoData = flowDynamicDataService.paresMapWithEcho(flowTask.getJvsAppId(), data, flowTask.getDataModelId());
        String taskTitleFormatStr = Optional.ofNullable(flowExtend).map(FlowExtendDto::getTaskTitleFormat).orElse("");
        // 未配置自定义流程标题格式，默认设置流程名称为标题
        if (ObjectNull.isNull(taskTitleFormatStr)) {
            return flowTask.getName();
        }
        // 将流程数据插入data
        echoData.put(SystemConstant.TASK_DATA_FIELD, flowTask);
        // 根据流程标题格式配置生成title
        return NoticeVariableUtils.replacementText(taskTitleFormatStr, echoData);
    }

    /**
     * 发通知
     */
    private void sendNotify() {
        FlowContext flowContext = FlowContextUtil.context().getContext();
        RuntimeData runtimeData = flowContext.getRuntimeData();
        FlowTask flowTask = runtimeData.getFlowTask();
        // 启动流程
        if (flowContext.getStart()) {
            applicationEventPublisher.publishEvent(new FlowNotifyEvent(this, flowTask, TriggerTypeEnum.FLOW_CREATED, null, TenantContextHolder.getTenantId()));
        }
        // 流程已结束，发送审批结果通知
        if (Boolean.FALSE.equals(FlowTaskStatusEnum.PENDING.equals(flowTask.getTaskStatus()))) {
            applicationEventPublisher.publishEvent(new FlowNotifyEvent(this, flowTask, TriggerTypeEnum.FLOW_APPROVAL_RESULTS, null, TenantContextHolder.getTenantId()));
        }
        // 待办任务通知
        if (ObjectNull.isNotNull(FlowTaskNodeUtil.getNodeIds())) {
            applicationEventPublisher.publishEvent(new FlowNotifyEvent(this, flowTask, TriggerTypeEnum.FLOW_APPROVAL_NODE, FlowTaskNodeUtil.getNodeIds(), TenantContextHolder.getTenantId()));
            applicationEventPublisher.publishEvent(new FlowNotifyEvent(this, flowTask, TriggerTypeEnum.FLOW_REMIND, FlowTaskNodeUtil.getNodeIds(), TenantContextHolder.getTenantId()));
        }
    }
}
