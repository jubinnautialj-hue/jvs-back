package cn.bctools.design.workflow.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.dto.FlowReqDto;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.entity.dto.*;
import cn.bctools.design.workflow.entity.enums.FlowTaskNodeApprovalTypeEnum;
import cn.bctools.design.workflow.entity.enums.ProcessStatusEnum;
import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import cn.bctools.design.workflow.mapper.FlowTaskNodeMapper;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.service.FlowTaskNodeService;
import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.context.FlowContext;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author zhuxiaokang
 * 工作流流转节点 服务实现类
 */
@Slf4j
@Service
public class FlowTaskNodeServiceImpl extends ServiceImpl<FlowTaskNodeMapper, FlowTaskNode> implements FlowTaskNodeService {

    /**
     * 需要保存处理结果的审批操作类型
     */
    private static final List<NodeOperationTypeEnum> SAVE_COURSES_NEXT_TYPE = Arrays.asList(NodeOperationTypeEnum.PASS, NodeOperationTypeEnum.REFUSE, NodeOperationTypeEnum.BACK, NodeOperationTypeEnum.REMOVE_SIGNER);

    @Override
    public FlowTaskNode saveResult(ProcessStatusEnum processStatusEnum) {
        FlowContext flowContext = FlowContextUtil.context().getContext();
        RuntimeData runtimeData = flowContext.getRuntimeData();
        // 节点id为空，开始节点，不保存数据，否则保存当前节点的处理结果
        if (runtimeData.getNodeId() == null) {
            return null;
        }
        // 保存当前节点处理结果
        List<FlowTaskNode> flowTaskNodes = list(Wrappers.<FlowTaskNode>lambdaQuery()
                .eq(FlowTaskNode::getFlowTaskId, runtimeData.getFlowTask().getId())
                .eq(FlowTaskNode::getNodeId, runtimeData.getNodeId())
                .eq(FlowTaskNode::getHang, Boolean.FALSE)
                .orderByDesc(FlowTaskNode::getCreateTime));
        if (CollectionUtils.isEmpty(flowTaskNodes)) {
            return null;
        }
        FlowTaskNode flowTaskNode = flowTaskNodes.get(0);

        // 保存审核结果
        flowTaskNode.setProcessStatus(processStatusEnum);
        assignCourse(flowTaskNode);
        updateById(flowTaskNode);
        return flowTaskNode;
    }

    @Override
    public void assignCourse(FlowTaskNode flowTaskNode) {
        FlowContext flowContext = FlowContextUtil.context().getContext();
        if (ObjectNull.isNull(flowContext)) {
            return;
        }
        FlowResult flowResult = Optional.ofNullable(flowContext.getFlowResult()).orElseGet(FlowResult::new);
        RuntimeData runtimeData = flowContext.getRuntimeData();
        UserDto userDto = runtimeData.getUser();
        // 保存审核结果（当前用户已保存过处理结果，不再保存）
        CourseDto courseDto = flowTaskNode.getCourse();
        // 当前审批的操作记录
        List<ApproveResultDto> currentApproveResults = runtimeData.getApproveResults();
        FlowReqDto flowDto = Optional.ofNullable(runtimeData.getFlowDto()).orElse(new FlowReqDto());
        NodeOperationTypeEnum nodeOperationTyp = flowDto.getNodeOperationType();
        if (SAVE_COURSES_NEXT_TYPE.contains(nodeOperationTyp) && ObjectNull.isNull(currentApproveResults)) {
            ApproveResultDto approveResultDto = new ApproveResultDto()
                    .setUserId(userDto.getId())
                    .setUserName(userDto.getRealName())
                    .setNodeOperationTypeEnum(nodeOperationTyp)
                    .setOpinion(flowDto.getOpinion())
                    .setTime(runtimeData.getTime())
                    .setBackNodeId(flowDto.getBackNodeId());
            courseDto.getApproveResultDtos().add(approveResultDto);
        } else {
            courseDto.getApproveResultDtos().addAll(currentApproveResults);
        }
        courseDto.setTime(runtimeData.getTime());
        // 保存节点数据数据版本
        courseDto.setDataVersion(runtimeData.getDataVersion());
        // 保存节点的通过率或数量
        if (ObjectNull.isNotNull(flowResult.getPassRate())) {
            courseDto.setPassRate(flowResult.getPassRate());
        }
        // 当前批次审批已结束，则设置所有审批结果为已结束
        if (Boolean.TRUE.equals(flowResult.isOver())) {
            flowTaskNode.getCourse().getApproveResultDtos().forEach(dto -> dto.setOver(Boolean.TRUE));
        }
        courseDto.setMode(runtimeData.getCurrentNodeMode());
        flowTaskNode.setCourse(courseDto);
        FlowContextUtil.refreshContext(flowTaskNode);
    }

    @Override
    public List<FlowTaskNode> getCurrentNodesByTaskId(String taskId) {
        return list(Wrappers.<FlowTaskNode>lambdaQuery()
                .eq(FlowTaskNode::getFlowTaskId, taskId)
                .eq(FlowTaskNode::getHang, Boolean.FALSE)
                .eq(FlowTaskNode::getProcessStatus, ProcessStatusEnum.PENDING));
    }

    @Override
    public FlowTaskNode getCurrentPendingNode(String taskId, String nodeId) {
        return getOne(Wrappers.<FlowTaskNode>lambdaQuery()
                .eq(FlowTaskNode::getFlowTaskId, taskId)
                .eq(FlowTaskNode::getNodeId, nodeId)
                .eq(FlowTaskNode::getHang, Boolean.FALSE)
                .eq(FlowTaskNode::getProcessStatus, ProcessStatusEnum.PENDING));
    }

    @Override
    public List<FlowTaskNode> getCurrentNodeByTaskIds(List<String> taskIds) {
        return  list(Wrappers.<FlowTaskNode>lambdaQuery()
                .in(FlowTaskNode::getFlowTaskId, taskIds)
                .eq(FlowTaskNode::getHang, Boolean.FALSE)
                .eq(FlowTaskNode::getProcessStatus, ProcessStatusEnum.PENDING));
    }

    @Override
    public List<FlowTaskNode> getCurrentNodes(String taskId, List<String> nodeIds) {
        return  list(Wrappers.<FlowTaskNode>lambdaQuery()
                .eq(FlowTaskNode::getFlowTaskId, taskId)
                .eq(FlowTaskNode::getHang, Boolean.FALSE)
                .eq(FlowTaskNode::getProcessStatus, ProcessStatusEnum.PENDING)
                .in(FlowTaskNode::getNodeId, nodeIds));
    }

    @Override
    public List<FlowTaskNode> getTaskNodes(String taskId, List<String> nodeIds) {
        return  list(Wrappers.<FlowTaskNode>lambdaQuery()
                .eq(FlowTaskNode::getFlowTaskId, taskId)
                .in(FlowTaskNode::getNodeId, nodeIds));
    }

    @Override
    public boolean whetherCurrentNode(String taskId, String nodeId) {
        long count = count(Wrappers.<FlowTaskNode>lambdaQuery()
                .eq(FlowTaskNode::getFlowTaskId, taskId)
                .eq(FlowTaskNode::getNodeId, nodeId)
                .eq(FlowTaskNode::getHang, Boolean.FALSE)
                .eq(FlowTaskNode::getProcessStatus, ProcessStatusEnum.PENDING));
        return count > 0;
    }

    @Override
    public FlowTaskNode saveNextNode(Node node, FlowTask flowTask) {
        return saveNextNode(node, flowTask, null);
    }

    @Override
    public FlowTaskNode saveNextNode(Node node, FlowTask flowTask, List<UserDto> userList) {
        String nodeId = node.getId();
        // 初始化下一节点处理信息
        CourseDto courseDto = new CourseDto();
        courseDto.setNodeName(node.getName());
        courseDto.setNodeId(nodeId);
        courseDto.setNodeType(node.getType());
        courseDto.setApproveResultDtos(new ArrayList<>());

        FlowTaskNode flowTaskNode = new FlowTaskNode();
        flowTaskNode.setFlowTaskId(flowTask.getId());
        flowTaskNode.setNodeId(nodeId);
        flowTaskNode.setCourse(courseDto);
        flowTaskNode.setProcessStatus(ProcessStatusEnum.PENDING);
        flowTaskNode.setJvsAppId(flowTask.getJvsAppId());
        flowTaskNode.setApprovalType(FlowTaskNodeApprovalTypeEnum.APPROVAL);
        flowTaskNode.setAppendApproval(null);
        flowTaskNode.setTempNewNode(null);
        flowTaskNode.setApprovalPersons(userList);
        // 若是启动流程，且未分配审批任务，则直接新增
        FlowContext flowContext = Optional.ofNullable(FlowContextUtil.context().getContext()).orElseGet(FlowContext::new);
        if (Boolean.TRUE.equals(flowContext.getStart()) && Boolean.FALSE.equals(flowContext.getApprovalTask())) {
            save(flowTaskNode);
        } else {
            FlowTaskNode taskNode = getOne(Wrappers.<FlowTaskNode>lambdaUpdate()
                    .eq(FlowTaskNode::getFlowTaskId, flowTaskNode.getFlowTaskId())
                    .eq(FlowTaskNode::getNodeId, flowTaskNode.getNodeId()));
            if (ObjectNull.isNotNull(taskNode)) {
                flowTaskNode.setId(taskNode.getId());
                updateById(flowTaskNode);
            } else {
                save(flowTaskNode);
            }
        }
        return flowTaskNode;
    }

    @Override
    public void saveTransfer(List<ProxyDto> proxyList, String taskId, String nodeId) {
        List<FlowTaskNode> flowTaskNodes = list(Wrappers.<FlowTaskNode>lambdaQuery()
                .eq(FlowTaskNode::getFlowTaskId, taskId)
                .eq(FlowTaskNode::getNodeId, nodeId)
                .orderByDesc(FlowTaskNode::getCreateTime));
        if (CollectionUtils.isEmpty(flowTaskNodes)) {
            return;
        }
        FlowTaskNode flowTaskNode = flowTaskNodes.get(0);
        CourseDto courseDto = flowTaskNode.getCourse();
        for (ProxyDto proxyDto : proxyList) {
            ApproveResultDto approveResultDto = new ApproveResultDto()
                    .setUserId(proxyDto.getUserId())
                    .setUserName(proxyDto.getUserName())
                    .setNodeOperationTypeEnum(NodeOperationTypeEnum.TRANSFER)
                    .setOpinion(new ApproveOpinionDto().setContent(proxyDto.getDirections()))
                    .setTime(proxyDto.getTime())
                    .setTransfer(proxyDto);
            courseDto.getApproveResultDtos().add(approveResultDto);
        }
        flowTaskNode.setCourse(courseDto);
        assignCourse(flowTaskNode);

        // 若当前是加签，则修改当前加签位置的审批人为代理审批人
        if (FlowTaskNodeApprovalTypeEnum.APPEND_APPROVAL.equals(flowTaskNode.getApprovalType())) {
            AppendApprovalDto appendApproval = flowTaskNode.getAppendApproval();
            appendApproval.getDetails().forEach(detail -> {
                if (appendApproval.getCurrentId().equals(detail.getId())) {
                    detail.getPersonnels().forEach(person -> {
                        Optional<ProxyDto> optionalProxy = proxyList.stream().filter(proxy -> proxy.getUserId().equals(person.getId())).findAny();
                        if (optionalProxy.isPresent()) {
                            ProxyDto proxy = optionalProxy.get();
                            person.setId(proxy.getProxyUserId()).setName(proxy.getProxyUserName());
                        }
                    });
                }
            });
        }
        // 若当前是审批，则修改当前节点的审批人为代理审批人
        if (FlowTaskNodeApprovalTypeEnum.APPROVAL.equals(flowTaskNode.getApprovalType())) {
            flowTaskNode.getApprovalPersons().forEach(user -> {
                Optional<ProxyDto> optionalProxy = proxyList.stream().filter(proxy -> proxy.getUserId().equals(user.getId())).findAny();
                if (optionalProxy.isPresent()) {
                    ProxyDto proxy = optionalProxy.get();
                    user.setId(proxy.getProxyUserId()).setRealName(proxy.getProxyUserName());
                }
            });
        }

        updateById(flowTaskNode);
    }


    @Override
    public void updateCourseApproveOver(FlowTaskNode flowTaskNode) {
        CourseDto courseDto = flowTaskNode.getCourse();
        courseDto.getApproveResultDtos().forEach(r -> r.setOver(Boolean.TRUE));
        flowTaskNode.setCourse(courseDto);
        updateById(flowTaskNode);
    }

    @Override
    public void removeTaskAll(String flowTaskId) {
        remove(Wrappers.<FlowTaskNode>lambdaQuery().eq(FlowTaskNode::getFlowTaskId, flowTaskId));
    }
}
