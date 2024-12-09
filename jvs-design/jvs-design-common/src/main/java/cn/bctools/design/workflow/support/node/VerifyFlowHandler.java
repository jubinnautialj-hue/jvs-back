package cn.bctools.design.workflow.support.node;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.dto.FlowReqDto;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.entity.dto.AppendApprovalDetailDto;
import cn.bctools.design.workflow.entity.dto.AppendApprovalDto;
import cn.bctools.design.workflow.entity.enums.FlowTaskNodeApprovalTypeEnum;
import cn.bctools.design.workflow.entity.enums.FlowTaskStatusEnum;
import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.AppendApprovalPointEnum;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.service.FlowDesignDynamicService;
import cn.bctools.design.workflow.service.FlowTaskApprovalRecordService;
import cn.bctools.design.workflow.service.FlowTaskNodeService;
import cn.bctools.design.workflow.service.TaskPersonService;
import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.enums.FlowNextTypeEnum;
import cn.bctools.design.workflow.support.function.dto.ModeDto;
import cn.bctools.design.workflow.support.function.dto.TransferRuntimeDto;
import cn.bctools.design.workflow.support.function.impl.*;
import cn.bctools.design.workflow.support.impl.AbstractFlowHandler;
import cn.bctools.design.workflow.utils.FlowApprovalCacheUtil;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import cn.bctools.design.workflow.utils.FlowUtil;
import cn.bctools.redis.utils.RedisUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 审批节点处理
 */
@Component
@AllArgsConstructor
public class VerifyFlowHandler extends AbstractFlowHandler {

    /**
     * 审批操作
     */
    private static final List<NodeOperationTypeEnum> APPROVAL_OPERATION = Arrays.asList(
            NodeOperationTypeEnum.PASS,
            NodeOperationTypeEnum.REFUSE,
            NodeOperationTypeEnum.BACK,
            NodeOperationTypeEnum.REMOVE_SIGNER);
    private final RedisUtils redisUtils;
    private final ModeFunction modeFunction;
    private final VerifyEventFunction verifyEventFunction;
    private final TaskPersonService taskPersonService;
    private final FlowTaskNodeService flowTaskNodeService;
    private final FlowTaskApprovalRecordService flowTaskApprovalRecordService;
    private final FlowDesignDynamicService flowDesignDynamicService;
    private final AppendApprovalFunction appendApprovalFunction;
    private final TransferFunction transferFunction;
    private final BackFunction backFunction;
    private final RemoveSignerFunction removeSignerFunction;

    @Override
    public NodeTypeEnum getType() {
        return NodeTypeEnum.SP;
    }

    @Override
    protected void handle(Node node, RuntimeData runtimeData) {
        // TRUE-触发事件，FALSE-不触发事件
        boolean trigger = Boolean.FALSE;
        NodeOperationTypeEnum nodeOperationType = runtimeData.getFlowDto().getNodeOperationType();
        FlowResult flowResult = new FlowResult();
        flowResult.setFlowNextTypeEnum(FlowNextTypeEnum.PENDING);

        // 移除审批人
        if (NodeOperationTypeEnum.REMOVE_SIGNER.equals(nodeOperationType)) {
            removeSignerFunction.invoke(node, runtimeData);
        }

        // 审批
        if (APPROVAL_OPERATION.contains(nodeOperationType)) {
            // 执行审批逻辑(同意/拒绝/回退)
            ModeDto modeResult = modeFunction.invoke(node, runtimeData);
            flowResult.setPassRate(modeResult.getPassRate());
            // 当前节点(审批或加签审批)审核结束
            if (Boolean.TRUE.equals(modeResult.getEnd())) {
                process(flowResult, modeResult.getPass(), node, runtimeData);
            } else {
                // 审批未结束，若有动态增加的节点，则临时保存该节点
                flowDesignDynamicService.addNode(Boolean.FALSE, runtimeData.getFlowTask(), runtimeData.getFlowExtend(), node, runtimeData.getAddNode());
            }
            // 保存工作流审批记录
            flowTaskApprovalRecordService.saveUnique(runtimeData.getFlowTask(), runtimeData.getUser());
            trigger = modeResult.getEnd();
        } else {
            // 处理不修改审批状态的操作类型
            otherOperationType(node, runtimeData, nodeOperationType);
            trigger = Boolean.TRUE;
        }

        // 触发事件
        triggerVerifyEvent(trigger, node, runtimeData);
        FlowContextUtil.refreshContext(flowResult);
    }

    /**
     * 不修改审批状态的操作类型
     *
     * @param node              节点
     * @param runtimeData       运行时数据
     * @param nodeOperationType 操作类型枚举
     */
    private void otherOperationType(Node node, RuntimeData runtimeData, NodeOperationTypeEnum nodeOperationType) {
        // 设置不修改当前审批人处理状态
        runtimeData.setChangePersonProcessStatus(Boolean.FALSE);
        // 转交
        if (NodeOperationTypeEnum.TRANSFER.equals(nodeOperationType)) {
            TransferRuntimeDto transfer = new TransferRuntimeDto(runtimeData.getUser(), runtimeData.getFlowTask(), runtimeData.getFlowDto().getTransfer(), null);
            transferFunction.invoke(node, transfer);
        }
        // 加签
        if (NodeOperationTypeEnum.APPEND.equals(nodeOperationType)) {
            appendApprovalFunction.invoke(node, runtimeData);
        }
    }

    /**
     * 审批结果处理
     * <p>
     * 拒绝：直接结束
     * <p>
     * 通过：
     * 审批结果生效：
     * 前加签： 流转到下一个节点
     * 后加签： 流转到下一个节点
     * 审批：   若有后加签，进入后加签，若无后加签，流转到下一个节点
     * <p>
     * 审批结果不生效：
     * 前加签：若有后续加签，进入后续加签，若无后续加签，当前审批节点审批
     * 后加签：若有后续加签，进入后续加签，若无后续加签，流转到下一个节点
     *
     * @param flowResult  流转结果
     * @param pass        TRUE-审批通过，FALSE-审批不通过
     * @param node        节点
     * @param runtimeData 运行时数据
     */
    private void process(FlowResult flowResult, Boolean pass, Node node, RuntimeData runtimeData) {
        flowResult.setOver(Boolean.TRUE);

        // 审批未通过（当前节点审批结束）
        if (Boolean.FALSE.equals(pass)) {
            notPass(runtimeData.getFlowDto(), flowResult);
            return;
        }

        // 获取当前加签配置
        FlowTaskNode flowTaskNode = runtimeData.getFlowTaskNode();
        AppendApprovalDto appendApproval = flowTaskNode.getAppendApproval();
        AppendApprovalDetailDto currentDetailDto = ObjectNull.isNull(appendApproval) ? null : appendApproval.getDetails().stream().filter(detail -> appendApproval.getCurrentId().equals(detail.getId())).findAny().get();

        // 审批已通过处理
        // 当前是非加签审批
        if (FlowTaskNodeApprovalTypeEnum.APPROVAL.equals(flowTaskNode.getApprovalType())) {
            // 有后加签
            if (ObjectNull.isNotNull(appendApproval) && AppendApprovalPointEnum.AFTER.equals(appendApproval.getAppendApprovalPoint())) {
                // 设置加签审批人为审批用户，审批节点依然是当前节点
                taskPersonService.saveTaskPerson(node, runtimeData, convertUsers(currentDetailDto));
                // 设置当前节点审批类型为“加签审批”，并修改加签配置
                updateNodeApprove(flowTaskNode, FlowTaskNodeApprovalTypeEnum.APPEND_APPROVAL, currentDetailDto.getId(), null);
                runtimeData.setChangePersonProcessStatus(Boolean.FALSE);
                // 清空当前节点审批结果缓存
                redisUtils.del(FlowApprovalCacheUtil.getApproverKey(flowTaskNode.getFlowTaskId(), flowTaskNode.getNodeId()));
            } else {
                // 无后加签，正常流转
                passed(flowResult, node, runtimeData);
            }
            return;
        }


        // 当前是加签审批
        if (FlowTaskNodeApprovalTypeEnum.APPEND_APPROVAL.equals(flowTaskNode.getApprovalType())) {
            // 获取后续加签配置
            AppendApprovalDetailDto nextDetailDto = appendApproval.getDetails().stream().filter(detail -> detail.getId().equals(currentDetailDto.getAfterId())).findAny().orElse(null);
            // 加签审批结果生效，任务流转到下一个节点
            if (Boolean.TRUE.equals(node.getProps().getAppendApproval().getValidApproval())) {
                passed(flowResult, node, runtimeData);
            } else {
                // 加签审批结果不生效
                // 若有后续加签审批，则进入下一个加签审批
                if (ObjectNull.isNotNull(nextDetailDto)) {
                    // 设置新的审批用户，审批节点依然是当前节点
                    taskPersonService.saveTaskPerson(node, runtimeData, convertUsers(nextDetailDto));
                    // 设置当前节点审批类型为“加签审批”，并修改加签配置
                    updateNodeApprove(flowTaskNode, FlowTaskNodeApprovalTypeEnum.APPEND_APPROVAL, nextDetailDto.getId(), appendApproval.getCurrentId());
                    runtimeData.setChangePersonProcessStatus(Boolean.FALSE);
                } else {
                    // 若无后续加签, 且是前加签审批
                    if (AppendApprovalPointEnum.BEFORE.equals(appendApproval.getAppendApprovalPoint())) {
                        // 设置当前审批节点审批人
                        taskPersonService.saveTaskPerson(node, runtimeData, runtimeData.getFlowTaskNode().getApprovalPersons());
                        // 设置当前节点审批类型为“审批”，并修改加签配置
                        updateNodeApprove(flowTaskNode, FlowTaskNodeApprovalTypeEnum.APPROVAL, null, null);
                        runtimeData.setChangePersonProcessStatus(Boolean.FALSE);
                    } else {
                        // 是后加签，流转到下一个节点
                        passed(flowResult, node, runtimeData);
                    }
                }

            }
        }
    }

    /**
     * 修改当前节点的审批类型、审批是否结束等信息
     *
     * @param flowTaskNode 任务节点
     * @param approvalType 审批类型
     * @param nextId       下一步加签id
     * @param currentId    当前加签id
     */
    public void updateNodeApprove(FlowTaskNode flowTaskNode, FlowTaskNodeApprovalTypeEnum approvalType, String nextId, String currentId) {
        // 将所有审批记录置为“审批结束”
        flowTaskNode.setApprovalType(approvalType);
        if (StringUtils.isBlank(nextId)) {
            flowTaskNode.setAppendApproval(null);
        } else {
            AppendApprovalDto appendApprovalDto = flowTaskNode.getAppendApproval();
            appendApprovalDto.getDetails().forEach(detail -> {
                if (detail.getId().equals(currentId)) {
                    detail.setOver(Boolean.TRUE);
                }
            });
            appendApprovalDto.setCurrentId(nextId);
            flowTaskNode.setAppendApproval(appendApprovalDto);
        }
        flowTaskNodeService.updateById(flowTaskNode);
    }

    /**
     * 审核已通过处理
     *
     * @param flowResult  流转结果
     * @param node        当前节点
     * @param runtimeData 运行时数据
     */
    private void passed(FlowResult flowResult, Node node, RuntimeData runtimeData) {
        // 若有动态增加的节点，则将该节点加入流程设计后，再继续流转
        flowDesignDynamicService.addNode(Boolean.TRUE, runtimeData.getFlowTask(), runtimeData.getFlowExtend(), node, runtimeData.getAddNode());
        flowResult.setNextNode(node);
    }

    /**
     * 审核未通过处理
     *
     * @param flowDto    工作流处理操作信息入参
     * @param flowResult 流转结果
     */
    private void notPass(FlowReqDto flowDto, FlowResult flowResult) {
        // 审核不通过
        switch (flowDto.getNodeOperationType()) {
            // 由于工作流支持投票制，且可配置达到投票阈值后，是否等待所有未审批人都处理完后再流转。所以，审批操作：“同意”、“拒绝”的最终结果是相同的
            case PASS:
            case REFUSE:
                flowResult.setFlowNextTypeEnum(FlowNextTypeEnum.END);
                flowResult.setFlowTaskStatus(FlowTaskStatusEnum.REJECTED);
                break;
            case BACK:
                flowResult.setPassRate(null);
                FlowContextUtil.refreshContext(flowResult);
                Node backNode = FlowUtil.findNode(flowDto.getBackNodeId());
                // 回退到发起人节点的处理进度中的节点名保存为“重新发起”
                if (NodeTypeEnum.ROOT.equals(backNode.getType())) {
                    backNode.setName("重新发起");
                }
                backFunction.invoke(backNode, null);
                flowResult.setFlowNextTypeEnum(FlowNextTypeEnum.NEXT);
                flowResult.setNode(backNode);
                break;
            default:
                break;
        }
    }


    /**
     * 触发事件
     *
     * @param trigger     TRUE-触发事件，FALSE-不触发事件
     * @param node        当前节点
     * @param runtimeData 运行时参数
     */
    private void triggerVerifyEvent(Boolean trigger, Node node, RuntimeData runtimeData) {
        // TRUE-触发事件，FALSE-不触发事件
        if (trigger) {
            verifyEventFunction.invoke(node, runtimeData);
        }
    }

    /**
     * 加签用户转user
     *
     * @param detailDto 加签信息
     * @return 加签用户
     */
    private List<UserDto> convertUsers(AppendApprovalDetailDto detailDto) {
        return detailDto.getPersonnels().stream()
                .map(personnel -> new UserDto().setId(personnel.getId()).setRealName(personnel.getName()))
                .collect(Collectors.toList());
    }

}
