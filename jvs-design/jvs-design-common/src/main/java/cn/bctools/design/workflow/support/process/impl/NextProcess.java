package cn.bctools.design.workflow.support.process.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.dto.FlowReqDto;
import cn.bctools.design.workflow.dto.FlowApprovalUserDTO;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.service.FlowTaskNodeService;
import cn.bctools.design.workflow.service.TaskPersonService;
import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.enums.FlowNextTypeEnum;
import cn.bctools.design.workflow.support.function.dto.BackResubmitDto;
import cn.bctools.design.workflow.support.function.dto.ParallelDto;
import cn.bctools.design.workflow.support.function.impl.BackResubmitFunction;
import cn.bctools.design.workflow.support.function.impl.ParallelFunction;
import cn.bctools.design.workflow.support.function.impl.TaskPersonFunction;
import cn.bctools.design.workflow.support.process.ProcessInterface;
import cn.bctools.design.workflow.support.process.ProcessResult;
import cn.bctools.design.workflow.support.process.ProcessTypeEnum;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import cn.bctools.design.workflow.utils.FlowTaskNodeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author zhuxiaokang
 * 当前节点处理完成，任务流转到下一节点（不自动执行） 结果处理
 */
@Slf4j
@Component
@AllArgsConstructor
public class NextProcess implements ProcessInterface {

    private final FlowTaskNodeService flowTaskNodeService;
    private final TaskPersonService taskPersonService;
    private final ParallelFunction parallelFunction;
    private final TaskPersonFunction taskPersonFunction;
    private final BackResubmitFunction backResubmitFunction;

    @Override
    public FlowNextTypeEnum getType() {
        return FlowNextTypeEnum.NEXT;
    }

    @Override
    public void execute(FlowResult flowResult) {
        RuntimeData runtimeData = FlowContextUtil.context().getRuntimeData();
        FlowTask flowTask = runtimeData.getFlowTask();

        // 若是回退操作，且回退到“发起人”节点，则保存下一步流转信息，且不自动执行
        FlowReqDto flowReq = Optional.ofNullable(runtimeData.getFlowDto()).orElseGet(FlowReqDto::new);
        if (NodeOperationTypeEnum.BACK.equals(flowReq.getNodeOperationType())) {
            List<FlowApprovalUserDTO> userDtos = null;
            if (NodeTypeEnum.ROOT.equals(flowResult.getNode().getType())) {
                UserDto rootUser = new UserDto().setId(flowTask.getCreateById()).setRealName(flowTask.getCreateBy());
                userDtos = Collections.singletonList(BeanCopyUtil.copy(rootUser, FlowApprovalUserDTO.class));
            }
            saveNextNode(flowResult, flowTask, userDtos);
            saveTaskPerson(flowResult.getNode(), runtimeData, userDtos);
            // 添加待发送通知的节点
            FlowTaskNodeUtil.addTaskNode(flowResult.getNode().getId());
            FlowContextUtil.refreshContext(ProcessResult.buildEndProcess());
            return;
        }

        // 非回退流转

        //如是回退后重新提交的，需要根据"被回退的数据重新提交配置"确定下一步流转的节点
        BackResubmitDto backResubmit = parseBackTaskResubmitNextNode(flowResult, runtimeData);

        // 若当前节点是并行节点，判断是否可继续流转
        ParallelDto parallelDto = new ParallelDto()
                .setFlowResult(flowResult)
                .setBackResubmitDto(backResubmit);
        if (Boolean.FALSE.equals(parallelFunction.invoke(flowResult.getNode(), parallelDto))) {
            // 添加待发送通知的节点
            FlowTaskNodeUtil.addTaskNode(runtimeData.getCurrentNode().getId());
            FlowContextUtil.refreshContext(ProcessResult.buildEndProcess());
            return;
        }
        // 根据节点类型处理下一步流转信息
        switch (flowResult.getNode().getType().getGroup()) {
            case MANUAL:
                // 人工节点，不自动执行，保存下一节点数据，保存下一节点待审批人集合
                List<FlowApprovalUserDTO> userDtos = taskPersonFunction.invoke(flowResult.getNode(), runtimeData);
                FlowTaskNode flowTaskNode = saveNextNode(flowResult, flowTask, userDtos);
                FlowContextUtil.refreshContext(flowTaskNode);
                saveTaskPerson(flowResult.getNode(), runtimeData, userDtos);
                FlowContextUtil.refreshApprovalTask(Boolean.TRUE);
                // 添加待发送通知的节点
                FlowTaskNodeUtil.addTaskNode(flowResult.getNode().getId());
                FlowContextUtil.refreshContext(ProcessResult.buildEndProcess());
                break;
            case AUTO:
                // 自动执行，且保存下级节点信息
                FlowTaskNode autoFlowTaskNode = saveNextNode(flowResult, flowTask);
                resetRuntimeData(runtimeData);
                FlowContextUtil.refreshContext(new ProcessResult().setProcessType(ProcessTypeEnum.NEXT).setNextNode(flowResult.getNode()));
                FlowContextUtil.refreshContext(autoFlowTaskNode);
                break;
            default:
                // 自动执行下一节点,不保存节点数据
                resetRuntimeData(runtimeData);
                FlowContextUtil.refreshContext(new ProcessResult().setProcessType(ProcessTypeEnum.NEXT).setNextNode(flowResult.getNode()));
                break;
        }
    }

    /**
     * 如是回退后重新提交的，需要根据"被回退的数据重新提交配置"确定下一步流转的节点
     *
     * @param flowResult 节点处理结果
     * @param runtimeData 运行时数据
     */
    private BackResubmitDto parseBackTaskResubmitNextNode(FlowResult flowResult, RuntimeData runtimeData) {
        // 根据“被回退的数据重新提交配置”得到下一步流转节点
        BackResubmitDto backResubmit = backResubmitFunction.invoke(null, runtimeData);
        if (ObjectNull.isNotNull(backResubmit.getNextNode())) {
            flowResult.setNode(backResubmit.getNextNode());
        }
        return backResubmit;
    }


    /**
     * 自动运行，重置部分运行时参数
     *
     * @param runtimeData 运行时数据
     */
    private void resetRuntimeData(RuntimeData runtimeData) {
        // 重置操作入参
        // 自动处理，不需要保存操作结果，所以置空
        FlowReqDto flowDto = runtimeData.getFlowDto();
        runtimeData.getApproveResults().clear();
        Optional.ofNullable(flowDto).map(f -> f.setNodeOperationType(null));
        runtimeData.setCurrentNodeMode(null);
        FlowContextUtil.refreshContext(runtimeData);
    }

    /**
     * 保存下一节点
     *
     * @param flowResult 节点处理结果
     * @param flowTask 运行时数据
     */
    private FlowTaskNode saveNextNode(FlowResult flowResult, FlowTask flowTask) {
        return flowTaskNodeService.saveNextNode(flowResult.getNode(), flowTask, null);
    }

    /**
     * 保存下一节点
     *
     * @param flowResult 节点处理结果
     * @param flowTask 运行时数据
     * @param users 当前节点审批人
     */
    private FlowTaskNode saveNextNode(FlowResult flowResult, FlowTask flowTask, List<FlowApprovalUserDTO> users) {
        List<UserDto> userDtos = null;
        if (ObjectNull.isNotNull(users)) {
            userDtos = BeanCopyUtil.copys(users, UserDto.class);
        }
        return flowTaskNodeService.saveNextNode(flowResult.getNode(), flowTask, userDtos);
    }

    /**
     * 保存下一节点待审批人
     *
     * @param nextNode    下一节点
     * @param runtimeData 运行时数据
     * @param users       指定用户为下一节点待审批人（若不为空，则以此字段数据为下一节点审批人）
     */
    private void saveTaskPerson(Node nextNode, RuntimeData runtimeData, List<FlowApprovalUserDTO> users) {
        taskPersonService.saveTaskPerson(nextNode, runtimeData, users);
    }
}
