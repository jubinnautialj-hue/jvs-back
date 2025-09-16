package cn.bctools.design.workflow.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.taskNotice.entity.FlowTaskNotice;
import cn.bctools.design.taskNotice.service.FlowTaskNoticeService;
import cn.bctools.design.workflow.dto.FlowApprovalUserDTO;
import cn.bctools.design.workflow.entity.FlowTaskPerson;
import cn.bctools.design.workflow.entity.dto.ProxyDto;
import cn.bctools.design.workflow.entity.dto.TransferDto;
import cn.bctools.design.workflow.entity.enums.ProcessStatusEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodePropertiesModeEnum;
import cn.bctools.design.workflow.service.FlowTaskPersonService;
import cn.bctools.design.workflow.service.TaskPersonService;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.context.FlowContext;
import cn.bctools.design.workflow.support.empty.CompositeApproverEmptyHandler;
import cn.bctools.design.workflow.support.function.dto.TransferRuntimeDto;
import cn.bctools.design.workflow.support.function.impl.TaskPersonFunction;
import cn.bctools.design.workflow.support.function.impl.TransferFunction;
import cn.bctools.design.workflow.support.listener.person.RemoveTaskPersonEvent;
import cn.bctools.design.workflow.support.timelimit.TimeLimitMessageHandler;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 工作流任务审批人服务
 */
@Service
@AllArgsConstructor
public class TaskPersonServiceImpl implements TaskPersonService {
    private final FlowTaskPersonService flowTaskPersonService;
    private final TaskPersonFunction taskPersonFunction;
    private final CompositeApproverEmptyHandler compositeApproverEmptyHandler;
    private final TransferFunction transferFunction;
    private final TimeLimitMessageHandler timeLimitMessageHandler;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final FlowTaskNoticeService flowTaskNoticeService;

    @Override
    public void saveTaskPerson(Node nextNode, RuntimeData runtimeData, List<FlowApprovalUserDTO> users) {
        // 获取下一节点审批人集合
        List<FlowApprovalUserDTO> userDtos = ObjectNull.isNotNull(users) ? users : taskPersonFunction.invoke(nextNode, runtimeData);
        // 下一节点审批人为空时处理
        userDtos = compositeApproverEmptyHandler.execute(userDtos, nextNode, runtimeData);
        if (CollectionUtils.isEmpty(userDtos)) {
            return;
        }

        // 去重
        userDtos = userDtos.stream().distinct().collect(Collectors.toList());
        // 获取代理
        List<String> transferUserIds = userDtos.stream().map(UserDto::getId).collect(Collectors.toList());
        TransferRuntimeDto transfer = new TransferRuntimeDto(runtimeData.getUser(), runtimeData.getFlowTask(), null, transferUserIds);
        List<ProxyDto> transfers = transferFunction.invoke(nextNode, transfer);
        // 保存下级节点待审批人
        List<FlowTaskPerson> flowTaskPersons = new ArrayList<>();
        int i = 0;
        for (FlowApprovalUserDTO user : userDtos) {
            FlowTaskPerson person = new FlowTaskPerson();
            person.setNodeId(nextNode.getId());
            person.setFlowTaskId(runtimeData.getFlowTask().getId());
            person.setUserId(user.getId());
            person.setUserName(user.getRealName());
            person.setProcessStatus(ProcessStatusEnum.PENDING);
            person.setTest(runtimeData.getFlowTask().getTest());
            // 依次审批
            if (NodePropertiesModeEnum.NEXT.equals(nextNode.getProps().getMode())) {
                if (ObjectNull.isNull(user.getApprovalSequence())) {
                    // 无审批顺序, 第一个用户设为待处理状态，其它用户设为准备中状态
                    if (i != 0) {
                        person.setProcessStatus(ProcessStatusEnum.PREPARE);
                    }
                    person.setNumber(i++);
                } else {
                    // 有审批顺序，则设置审批顺序不等于1的用户为准备中
                    if (user.getApprovalSequence() != 1) {
                        person.setProcessStatus(ProcessStatusEnum.PREPARE);
                    }
                    person.setNumber(user.getApprovalSequence());
                }
            }
            person.setJvsAppId(runtimeData.getFlowTask().getJvsAppId());
            // 转交
            transfers(person, transfers);

            flowTaskPersons.add(person);
        }
        // 若是启动流程，且未分配审批任务，则直接新增
        FlowContext flowContext = FlowContextUtil.context().getContext();
        if (Boolean.TRUE.equals(flowContext.getStart()) && Boolean.FALSE.equals(flowContext.getApprovalTask())) {
            flowTaskPersonService.saveBatch(flowTaskPersons);
        } else {
            // 先删除当前节点的所有审批人，再保存下一节点的审批人
            List<String> removeTaskPersonIds = flowTaskPersonService
                    .list(Wrappers.<FlowTaskPerson>lambdaQuery()
                            .eq(FlowTaskPerson::getFlowTaskId, runtimeData.getFlowTask().getId())
                            .eq(FlowTaskPerson::getNodeId, runtimeData.getCurrentNode().getId()))
                            .stream().map(FlowTaskPerson::getId).collect(Collectors.toList());
            applicationEventPublisher.publishEvent(new RemoveTaskPersonEvent(this, removeTaskPersonIds));
            flowTaskPersonService.saveBatch(flowTaskPersons);
            List<String> removeBizTaskIds= flowTaskNoticeService.list(Wrappers.<FlowTaskNotice>lambdaQuery().in(FlowTaskNotice::getTaskId, removeTaskPersonIds))
                    .stream().map(FlowTaskNotice::getBizTaskId).collect(Collectors.toList());
            //2025.09.10 关闭已完成的待办提醒通知
            flowTaskNoticeService.close(runtimeData.getFlowTask(),removeBizTaskIds);
        }
        //2025.09.08 发送待办提醒通知
        flowTaskNoticeService.create(runtimeData.getFlowTask(), nextNode, flowTaskPersons);
        // 发送延时任务（校验审核是否超时等功能）
        timeLimitMessageHandler.delayedTask(nextNode, runtimeData.getFlowTask(), flowTaskPersons);
    }

    /**
     * 任务转交
     *
     * @param person 审批人
     * @param transfers 转交人集合
     */
    private void transfers(FlowTaskPerson person, List<ProxyDto> transfers) {
        if (CollectionUtils.isEmpty(transfers)) {
            return;
        }
        // 当前审批人转交任务给代理人
        TransferDto transferDto = transfers.stream().filter(transfer -> transfer.getUserId().equals(person.getUserId())).findFirst().orElse(null);
        if (ObjectNull.isNull(transferDto)) {
            return;
        }
        person.setUserId(transferDto.getProxyUserId());
        person.setUserName(transferDto.getProxyUserName());
    }
}
