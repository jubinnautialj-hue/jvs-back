package cn.bctools.design.workflow.support.process.impl;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.entity.FlowTaskPerson;
import cn.bctools.design.workflow.entity.enums.ProcessStatusEnum;
import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import cn.bctools.design.workflow.model.enums.NodePropertiesModeEnum;
import cn.bctools.design.workflow.service.FlowTaskPersonService;
import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.enums.FlowNextTypeEnum;
import cn.bctools.design.workflow.support.process.ProcessInterface;
import cn.bctools.design.workflow.support.process.ProcessResult;
import cn.bctools.design.workflow.support.timelimit.TimeLimitMessageHandler;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import cn.bctools.design.workflow.utils.FlowTaskNodeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author zhuxiaokang
 * 审批阶段结果
 */
@Slf4j
@Component
@AllArgsConstructor
public class PendingProcess implements ProcessInterface {

    private final FlowTaskPersonService flowTaskPersonService;
    private final TimeLimitMessageHandler timeLimitMessageHandler;

    @Override
    public FlowNextTypeEnum getType() {
        return FlowNextTypeEnum.PENDING;
    }

    @Override
    public void execute(FlowResult flowResult) {
        RuntimeData runtimeData = FlowContextUtil.context().getRuntimeData();
        log.debug("【阶段结果处理】节点：{}", runtimeData.getNodeId());
        // 设置待办人已处理
        updateFlowTaskPersonStatus(runtimeData);
        // 若审批模式为按选择顺序依次审批，则设置下一个处理人状态
        updateNextPersonStatus(runtimeData);
        FlowContextUtil.refreshContext(ProcessResult.buildEndProcess());
    }

    /**
     * 设置待办人已处理
     *
     * @param runtimeData 运行时数据
     */
    private void updateFlowTaskPersonStatus(RuntimeData runtimeData) {
        if (Boolean.FALSE.equals(runtimeData.getChangePersonProcessStatus())) {
            return;
        }
        // 忽略不修改待办状态的操作
        if (NodeOperationTypeEnum.REMOVE_SIGNER.equals(runtimeData.getFlowDto().getNodeOperationType())) {
            return;
        }
        // 增加了“转交功能”，“代理功能”，一个任务节点中可能存在多个相同的审批用户。 处理时，任选其一处理即可
        List<FlowTaskPerson> flowTaskPersons = flowTaskPersonService.list(Wrappers.<FlowTaskPerson>lambdaQuery()
                .eq(FlowTaskPerson::getFlowTaskId, runtimeData.getFlowTask().getId())
                .eq(FlowTaskPerson::getNodeId, runtimeData.getNodeId())
                .eq(FlowTaskPerson::getUserId, runtimeData.getUser().getId())
                .ne(FlowTaskPerson::getProcessStatus, ProcessStatusEnum.PROCESSED));
        if (CollectionUtils.isEmpty(flowTaskPersons)) {
            return;
        }
        FlowTaskPerson flowTaskPerson = flowTaskPersons.get(0);
        flowTaskPerson.setProcessStatus(ProcessStatusEnum.PROCESSED);
        flowTaskPersonService.updateById(flowTaskPerson);
    }

    /**
     * 若审批模式为按选择顺序依次审批，则设置下一个审批人状态
     *
     * @param runtimeData 运行时参数
     */
    private void updateNextPersonStatus(RuntimeData runtimeData) {
        if (Boolean.FALSE.equals(runtimeData.getChangePersonProcessStatus())) {
            return;
        }
        if (!NodePropertiesModeEnum.NEXT.equals(runtimeData.getCurrentNode().getProps().getMode())) {
            return;
        }
        // 若有待审批人，则不设置下一个准备中的用户为审批人
        List<FlowTaskPerson> flowTaskPersons = flowTaskPersonService.list(Wrappers.<FlowTaskPerson>lambdaQuery()
                .eq(FlowTaskPerson::getFlowTaskId, runtimeData.getFlowTask().getId())
                .eq(FlowTaskPerson::getNodeId, runtimeData.getNodeId())
                .eq(FlowTaskPerson::getProcessStatus, ProcessStatusEnum.PENDING));
        if (ObjectNull.isNotNull(flowTaskPersons)) {
            return;
        }
        // 得到第一个准备中的审批人
        FlowTaskPerson prepareFlowTaskPerson = flowTaskPersonService.getOne(Wrappers.<FlowTaskPerson>lambdaQuery()
                .eq(FlowTaskPerson::getProcessStatus, ProcessStatusEnum.PREPARE)
                .eq(FlowTaskPerson::getFlowTaskId, runtimeData.getFlowTask().getId())
                .eq(FlowTaskPerson::getNodeId, runtimeData.getNodeId())
                .orderByAsc(FlowTaskPerson::getNumber).last("limit 1"));
        if (ObjectUtil.isEmpty(prepareFlowTaskPerson)) {
            return;
        }
        // 设置状态为“待处理"
        prepareFlowTaskPerson.setProcessStatus(ProcessStatusEnum.PENDING);
        flowTaskPersonService.updateById(prepareFlowTaskPerson);
        // 发送延时任务（校验审核是否超时等功能）
        timeLimitMessageHandler.delayedTask(runtimeData.getCurrentNode(), runtimeData.getFlowTask(), Collections.singletonList(prepareFlowTaskPerson));
        // 添加待发送通知的节点
        FlowTaskNodeUtil.addTaskNode(runtimeData.getCurrentNode().getId());
    }
}
