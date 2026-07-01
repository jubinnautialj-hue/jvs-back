package cn.bctools.design.workflow.support.function.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.dto.FlowReqDto;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.entity.dto.ApproveOpinionDto;
import cn.bctools.design.workflow.entity.dto.ApproveResultDto;
import cn.bctools.design.workflow.entity.dto.CourseDto;
import cn.bctools.design.workflow.entity.dto.FlowExtendDto;
import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.model.properties.AutoApproval;
import cn.bctools.design.workflow.service.FlowDesignService;
import cn.bctools.design.workflow.service.FlowTaskNodeService;
import cn.bctools.design.workflow.service.FlowTaskPathService;
import cn.bctools.design.workflow.service.FlowTaskPersonService;
import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.enums.FlowNextTypeEnum;
import cn.bctools.design.workflow.support.function.AbstractFunctionHandler;
import cn.bctools.design.workflow.support.function.dto.AutoApprovalDto;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 自动审批
 */
@Slf4j
@Component
@AllArgsConstructor
public class AutoApprovalFunction extends AbstractFunctionHandler<AutoApprovalDto, FlowResult> {
    private final FlowDesignService flowDesignService;
    private final FlowTaskPersonService flowTaskPersonService;
    private final FlowTaskPathService flowTaskPathService;
    private final FlowTaskNodeService flowTaskNodeService;

    @Override
    public AutoApprovalDto invoke(Node currentNode, FlowResult flowResult) {
        Node nextNode = null;
        if (FlowNextTypeEnum.NEXT.equals(flowResult.getFlowNextTypeEnum())) {
            nextNode = flowResult.getNode();
        }
        if (FlowNextTypeEnum.PENDING.equals(flowResult.getFlowNextTypeEnum())) {
            nextNode = currentNode;
        }
        if (ObjectNull.isNull(nextNode) || Boolean.FALSE.equals(NodeTypeEnum.SP.equals(nextNode.getType()))) {
            return new AutoApprovalDto();
        }
        RuntimeData runtimeData = FlowContextUtil.context().getRuntimeData();
        // 获取自动审批规则
        AutoApproval autoApproval = getAutoApproval(nextNode, runtimeData);
        if (ObjectNull.isNull(autoApproval)) {
            return new AutoApprovalDto();
        }
        // 得到自动审批参数
        return autoExecuteParam(autoApproval, nextNode, runtimeData);
    }

    /**
     * 获取自动审批规则
     *
     * @param nextNode     下一步流转的审批节点
     * @param runtimeData  运行时数据
     * @return 人工审批节点自动审批规则
     */
    private AutoApproval getAutoApproval(Node nextNode, RuntimeData runtimeData) {
        // 节点自动审批配置优先
        AutoApproval nodeAutoApproval = Optional.ofNullable(nextNode.getProps().getAutoApproval()).orElse(new AutoApproval());
        if (Boolean.TRUE.equals(nodeAutoApproval.getSelfAuto()) || Boolean.TRUE.equals(nodeAutoApproval.getAdjacentNode())) {
            return nodeAutoApproval;
        }
        // 全局自动审批配置
        AutoApproval globalAutoApproval = Optional.ofNullable(runtimeData.getFlowExtend()).orElse(new FlowExtendDto()).getAutoApproval();
        // true-未启用全局自动审批配置，false-开启了全局自动审批配置
        // 未开启全局自动审批 || (未启用发起人自动审批 && 未启用相邻节点自动审批)
        boolean notEnabledGlobal = ObjectNull.isNull(globalAutoApproval) || (Boolean.FALSE.equals(globalAutoApproval.getSelfAuto()) && Boolean.FALSE.equals(globalAutoApproval.getAdjacentNode()));
        if (notEnabledGlobal) {
            return null;
        }
        return globalAutoApproval;
    }

    /**
     * 得到自动审批参数
     *
     * @param autoApproval 自动审批规则
     * @param nextNode     下一审批节点
     * @param runtime      运行时数据
     */
    private AutoApprovalDto autoExecuteParam(AutoApproval autoApproval, Node nextNode, RuntimeData runtime) {
        FlowTask flowTask = runtime.getFlowTask();
        // 得到下一节点待审批人
        List<UserDto> nextNodePendingUsers = flowTaskPersonService.getPendingApproveUsers(flowTask.getId(), nextNode.getId());
        if (CollectionUtils.isEmpty(nextNodePendingUsers)) {
            return new AutoApprovalDto();
        }
        Set<UserDto> autoReqUsers = new HashSet<>();
        // 下一节点待审批人有发起人时, 自动通过
        if (autoApproval.getSelfAuto() && nextNodePendingUsers.stream().anyMatch(u -> u.getId().equals(flowTask.getCreateById()))) {
            autoReqUsers.add(new UserDto().setId(flowTask.getCreateById()).setRealName(flowTask.getCreateBy()));
        }
        // 下一节点待审批人与当前节点已审批人相同的，自动审批
        if (autoApproval.getAdjacentNode()) {
            adjacentNode(autoReqUsers, flowTask, nextNode, nextNodePendingUsers);
        }
        if (CollectionUtils.isEmpty(autoReqUsers)) {
            return new AutoApprovalDto();
        }
        // 封装用于发起自动审批的数据
        FlowReqDto flowReqDto = new FlowReqDto()
                .setId(flowTask.getId())
                .setData(runtime.getData())
                .setNodeId(nextNode.getId())
                .setNodeOperationType(NodeOperationTypeEnum.PASS)
                .setOpinion(new ApproveOpinionDto().setContent("自动审批"));
        FlowTaskNode currentTaskNode = flowTaskNodeService.getCurrentPendingNode(flowReqDto.getId(), flowReqDto.getNodeId());
        FlowExtendDto flowExtend = flowDesignService.getFlowExtend(flowTask.getFlowDesignId());
        RuntimeData runtimeData = new RuntimeData()
                .setNodeId(nextNode.getId())
                .setCurrentNode(nextNode)
                .setFlowDto(flowReqDto)
                .setData(runtime.getData())
                .setFlowTaskNodes(runtime.getFlowTaskNodes())
                .setTaskPaths(runtime.getTaskPaths())
                .setFlowTaskNode(currentTaskNode)
                .setFlowExtend(flowExtend)
                .setFlowTask(flowTask);
        List<RuntimeData> autoTasks = new ArrayList<>();
        autoReqUsers.forEach(userDto -> {
            if (flowTaskPersonService.checkPendingTask(flowTask.getId(), nextNode.getId(), userDto.getId())) {
                runtimeData.setUser(userDto);
                autoTasks.add(runtimeData);
            }
        });
        return new AutoApprovalDto().setEnable(Boolean.TRUE).setAutoTasks(autoTasks);
    }

    /**
     * 封装相邻节点自动审批参数
     *
     * @param autoReqUsers         自动审批人集合
     * @param flowTask             任务
     * @param nextNode             下一审批节点
     * @param nextNodePendingUsers 得到下一节点待审批人
     */
    private void adjacentNode(Set<UserDto> autoReqUsers, FlowTask flowTask, Node nextNode, List<UserDto> nextNodePendingUsers) {
        List<String> userIds = getUserIds(nextNode, flowTask);
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        nextNodePendingUsers.stream().filter(pendingUser -> userIds.contains(pendingUser.getId()))
                .forEach(user -> autoReqUsers.add(new UserDto().setId(user.getId()).setRealName(user.getRealName())));
    }

    /**
     * 获取相邻节点已审批人
     *
     * @param nextNode 下一审批节点
     * @param flowTask 任务
     * @return 已审批用户id
     */
    private List<String> getUserIds(Node nextNode, FlowTask flowTask) {
        List<CourseDto> spCourses = getCourses(flowTask);
        if (CollectionUtils.isEmpty(spCourses)) {
            return Collections.emptyList();
        }
        // 得到下一个节点所在路径，并获取该节点在各个路径中的前一个审批节点id
        List<List<Node>> paths = flowTaskPathService.getNodePaths(flowTask, nextNode.getId());
        List<String> prevManualNodeIds = paths.stream().map(path -> {
            Node prevManualNode = null;
            for (Node node : path) {
                if (NodeTypeEnum.SP.equals(node.getType())) {
                    if (nextNode.getId().equals(node.getId())) {
                        break;
                    }
                    prevManualNode = node;
                }
            }
            return prevManualNode;
        }).filter(Objects::nonNull).map(Node::getId).distinct().collect(Collectors.toList());

        // 得到前面的审批节点最后一次审批结果
        List<CourseDto> lastCourses = prevManualNodeIds.stream().map(prevNodeId -> {
            List<CourseDto> courseDtos = spCourses.stream().filter(course -> prevNodeId.equals(course.getNodeId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(courseDtos)) {
                return null;
            }
            return courseDtos.get(courseDtos.size() - 1);
        }).filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(lastCourses)) {
            return Collections.emptyList();
        }
        // 得到已审批用户id
        return lastCourses.stream()
                .flatMap(course ->
                        course.getApproveResultDtos().stream()
                                .filter(result -> NodeOperationTypeEnum.PASS.equals(result.getNodeOperationTypeEnum()))
                                .map(ApproveResultDto::getUserId)
                                .collect(Collectors.toList()).stream()
                ).distinct().collect(Collectors.toList());
    }

    /**
     * 得到审批节点处理结果
     *
     * @param flowTask 任务
     * @return 指定任务的审批过程
     */
    private List<CourseDto> getCourses(FlowTask flowTask) {
        List<CourseDto> spCourses = flowTask.getCourses();
        if (CollectionUtils.isEmpty(spCourses)) {
            return Collections.emptyList();
        }
        // 若工作流重新启动过，则获取最后一个ROOT节点之后的审批结果
        Optional<CourseDto> rootCourse = spCourses.stream().filter(cc -> NodeTypeEnum.ROOT.equals(cc.getNodeType())).findFirst();
        if (rootCourse.isPresent()) {
            return spCourses.subList(spCourses.lastIndexOf(rootCourse.get()), spCourses.size());
        } else {
            // 工作流未重启过，则获取最后一个节点的审批结果
            return spCourses;
        }
    }
}
