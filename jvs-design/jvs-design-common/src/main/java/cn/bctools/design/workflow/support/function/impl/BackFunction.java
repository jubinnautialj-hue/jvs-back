package cn.bctools.design.workflow.support.function.impl;

import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.entity.FlowTaskParallel;
import cn.bctools.design.workflow.entity.FlowTaskPerson;
import cn.bctools.design.workflow.entity.dto.CourseDto;
import cn.bctools.design.workflow.entity.dto.FlowManualNode;
import cn.bctools.design.workflow.entity.dto.ParallelBranchDto;
import cn.bctools.design.workflow.enums.BackTaskResubmitEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.model.enums.NodeTypeGroupEnum;
import cn.bctools.design.workflow.model.properties.BackProperties;
import cn.bctools.design.taskNotice.entity.FlowTaskNotice;
import cn.bctools.design.taskNotice.service.FlowTaskNoticeService;
import cn.bctools.design.workflow.service.*;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.function.AbstractFunctionHandler;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import cn.bctools.design.workflow.utils.FlowUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 回退处理
 */
@Slf4j
@Component
@AllArgsConstructor
public class BackFunction extends AbstractFunctionHandler<Boolean, RuntimeData> {

    private final FlowTaskService flowTaskService;
    private final FlowTaskPathService flowTaskPathService;
    private final FlowTaskParallelService flowTaskParallelService;
    private final FlowTaskNodeService flowTaskNodeService;
    private final FlowTaskPersonService flowTaskPersonService;
    private final FlowTaskNoticeService flowTaskNoticeService;

    @Override
    public Boolean invoke(Node backNode, RuntimeData runtimeData) {
        runtimeData = FlowContextUtil.context().getRuntimeData();
        FlowTask flowTask = runtimeData.getFlowTask();
        // 获取回退目标节点所有可执行路径
        List<List<Node>> paths = flowTaskPathService.getNodePaths(flowTask, backNode.getId());
        // 获取提交回退操作的节点的回退规则配置
        Node sendBackNode = runtimeData.getCurrentNode();
        BackProperties backProperties = FlowUtil.getNodeBackProps(sendBackNode);
        // 回退操作相关信息
        BackDto backDto = convertBack(paths, backNode);
        // 重置修改任务信息
        backFlowTask(flowTask, backProperties, backDto.getChildManualNodeIds());
        // 回退并行任务信息
        backParallel(flowTask, backNode, backProperties, backDto.getParentParallelBranchIds(), backDto.getChildParallelBranchIds());
        // 重置回退目标节点的所有下级节点待办信息
        backTask(flowTask, backProperties, backDto.getChildNodeIds());
        // 退回操作：关闭当前任务下所有未处理的待办通知
        List<String> removeBizTaskIds = flowTaskNoticeService.list(Wrappers.<FlowTaskNotice>lambdaQuery()
                        .eq(FlowTaskNotice::getInstanceId, flowTask.getId())
                        .eq(FlowTaskNotice::getNodeId, sendBackNode.getId())
                        .eq(FlowTaskNotice::getStatus, 0))
                .stream().map(FlowTaskNotice::getBizTaskId).collect(Collectors.toList());
        if (removeBizTaskIds != null && !removeBizTaskIds.isEmpty()) {
            flowTaskNoticeService.close(flowTask, removeBizTaskIds);
        }
        return Boolean.TRUE;
    }

    /**
     * 重置修改任务信息
     *
     * @param flowTask 工作流任务
     * @param backProperties 回退规则配置
     * @param childManualNodeIds  回退目标节点所在路径下所有人工节点id
     */
    private void backFlowTask(FlowTask flowTask, BackProperties backProperties, Set<String> childManualNodeIds) {
        // 重置已处理人工节点信息
        switch (backProperties.getResubmit()) {
            case SEQUENCE:
                // 回退后重新发起规则为按流程顺序审批，则重置已处理人工节点信息
                LinkedList<FlowManualNode> flowManualNodes = flowTask.getFlowManualNodes();
                flowManualNodes.removeIf(node -> childManualNodeIds.contains(node.getId()));
                break;
            case DIRECT_CURRENT_NODE:
                // 回退后重新发起规则为直达当前（发起回退的）节点，则不重置已处理人工节点信息
            default:
                break;
        }

        // 将回退操作加入审批结果
        RuntimeData runtimeData = FlowContextUtil.context().getRuntimeData();
        FlowTaskNode flowTaskNode = runtimeData.getFlowTaskNode();
        flowTaskNodeService.assignCourse(flowTaskNode);
        LinkedList<CourseDto> courseDtos = Optional.ofNullable(flowTask.getCourses()).orElse(new LinkedList<>());
        courseDtos.add(flowTaskNode.getCourse());
        flowTask.setCourses(courseDtos);

        flowTaskService.updateById(flowTask);
    }


    /**
     * 修改并行任务信息
     *
     * @param flowTask 工作流任务
     * @param backNode 回退目标节点
     * @param backProperties 发起回退操作的节点的回退规则配置
     * @param parentParallelBranchIds 回退目标节点所在路径所有上级并行分支节点id
     * @param childParallelBranchIds 回退目标节点所在路径所有下级并行分支节点id
     */
    private void backParallel(FlowTask flowTask, Node backNode, BackProperties backProperties, Set<String> parentParallelBranchIds, Set<String> childParallelBranchIds) {
        // 回退后重新发起规则为直达当前（发起回退的）节点，则不修改并行任务信息
        if (BackTaskResubmitEnum.DIRECT_CURRENT_NODE.equals(backProperties.getResubmit())) {
            return;
        }

        // 获取并行信息
        List<FlowTaskParallel> flowTaskParallels = flowTaskParallelService.list(Wrappers.<FlowTaskParallel>lambdaQuery().eq(FlowTaskParallel::getFlowTaskId, flowTask.getId()));
        if (CollectionUtils.isEmpty(flowTaskParallels)) {
            return;
        }
        // 重置回退目标节点的所有下级并行分支进度
        updateParallel(flowTaskParallels, childParallelBranchIds);

        // 获取回退目标节点所在并行分支
        Node branch = FlowUtil.getParallelBranchNode(backNode);
        if (FlowUtil.isNullNode(branch)) {
            // 不是并行节点，不处理并行任务
            return;
        }
        // 并行分支已结束，则还需回退处理上级并行任务
        boolean branchComplete = flowTaskParallels.stream().flatMap(flowTaskParallel -> flowTaskParallel.getBranchs().stream()).anyMatch(branchNode -> branchNode.getBranchId().equals(branch.getId()) && branchNode.getComplete());
        if (branchComplete) {
            // 回退目标节点所在分支及所有上级分支状态
            updateParallel(flowTaskParallels, parentParallelBranchIds);
        }

    }

    /**
     * 回退并行分支进度
     *
     * @param flowTaskParallels 并行分支
     * @param branchIds 并行分支id集合
     */
    private void updateParallel(List<FlowTaskParallel> flowTaskParallels, Set<String> branchIds) {
        List<FlowTaskParallel> updateChildParallels = flowTaskParallels.stream().filter(parallel -> parallel.getBranchs().stream().anyMatch(branch -> branchIds.contains(branch.getBranchId())))
                .map(branch -> {
                    List<ParallelBranchDto> newBranchs = branch.getBranchs().stream().peek(b -> {
                        if (branchIds.contains(b.getBranchId())) {
                            b.setComplete(Boolean.FALSE);
                        }
                    }).collect(Collectors.toList());
                    long completedNumber = newBranchs.stream().filter(ParallelBranchDto::getComplete).count();
                    branch.setCompletedNumber((int) completedNumber);
                    branch.setBranchs(newBranchs);
                    return branch;
                }).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(updateChildParallels)) {
            flowTaskParallelService.updateBatchById(updateChildParallels);
        }
    }

    /**
     * 回退待办任务
     *
     * @param flowTask 工作流任务
     * @param backProperties 发起回退操作的节点的回退规则配置
     * @param childNodeIds 回退目标节点所在路径所有下级节点id
     */
    private void backTask(FlowTask flowTask, BackProperties backProperties, Set<String> childNodeIds) {
        // 回退后重新发起规则为按流程顺序审批，直接删除待办相关数据
        if (BackTaskResubmitEnum.SEQUENCE.equals(backProperties.getResubmit())) {
            // 删除节点信息
            flowTaskNodeService.remove(Wrappers.<FlowTaskNode>lambdaQuery().eq(FlowTaskNode::getFlowTaskId, flowTask.getId()).in(FlowTaskNode::getNodeId, childNodeIds));
            // 删除待办人信息
            flowTaskPersonService.remove(Wrappers.<FlowTaskPerson>lambdaQuery().eq(FlowTaskPerson::getFlowTaskId, flowTask.getId()).in(FlowTaskPerson::getNodeId, childNodeIds));
        }

        // 回退后重新发起规则为直达当前（发起回退的）节点，挂起待办相关数据
        if (BackTaskResubmitEnum.DIRECT_CURRENT_NODE.equals(backProperties.getResubmit())) {
            // 挂起节点
            flowTaskNodeService.update(Wrappers.<FlowTaskNode>lambdaUpdate()
                    .set(FlowTaskNode::getHang, Boolean.TRUE)
                    .eq(FlowTaskNode::getFlowTaskId, flowTask.getId())
                    .in(FlowTaskNode::getNodeId, childNodeIds));
            // 挂起待办人
            flowTaskPersonService.update(Wrappers.<FlowTaskPerson>lambdaUpdate()
                    .set(FlowTaskPerson::getHang, Boolean.TRUE)
                    .eq(FlowTaskPerson::getFlowTaskId, flowTask.getId())
                    .in(FlowTaskPerson::getNodeId, childNodeIds));
        }

    }

    /**
     * 封装回退操作相关数据
     *
     * @param paths 路径集合
     * @param node 指定节点
     *
     * @return 回退操作相关数据
     */
    private BackDto convertBack(List<List<Node>> paths, Node node) {
        BackDto backDto = new BackDto();
        paths.forEach(path -> {
            boolean addParent = true;
            for (Node n : path) {
                if (node.getId().equals(n.getId())) {
                    backDto.addChildManualNodeIds(node);
                    addParent = false;
                    continue;
                }
                if (addParent) {
                    backDto.addParentParallelBranchIds(n);
                } else {
                    backDto.addChildParallelBranchIds(n);
                    backDto.addChildManualNodeIds(n);
                    backDto.addNodeIds(n);
                }
            }
        });
        return backDto;
    }

    private static class BackDto {
        /**
         * 回退目标节点所在路径所有上级并行分支节点id
         */
        private Set<String> parentParallelBranchIds = new HashSet<>();
        /**
         * 回退目标节点所在路径所有下级并行分支节点id
         */
        private Set<String> childParallelBranchIds = new HashSet<>();
        /**
         * 回退目标节点所在路径所有下级节点id
         */
        private Set<String> childNodeIds = new HashSet<>();
        /**
         * 回退目标节点所在路径下所有人工节点id
         */
        private Set<String> childManualNodeIds = new HashSet<>();

        public Set<String> getParentParallelBranchIds() {
            return parentParallelBranchIds;
        }

        public Set<String> getChildParallelBranchIds() {
            return childParallelBranchIds;
        }

        public Set<String> getChildNodeIds() {
            return childNodeIds;
        }

        public Set<String> getChildManualNodeIds() {
            return childManualNodeIds;
        }

        public void addParentParallelBranchIds(Node node) {
            if (NodeTypeEnum.PB.equals(node.getType())) {
                parentParallelBranchIds.add(node.getId());
            }
        }

        public void addChildParallelBranchIds(Node node) {
            if (NodeTypeEnum.PB.equals(node.getType())) {
                childParallelBranchIds.add(node.getId());
            }
        }

        public void addNodeIds(Node node) {
            childNodeIds.add(node.getId());
        }

        public void addChildManualNodeIds(Node node) {
            if (NodeTypeGroupEnum.MANUAL.equals(node.getType().getGroup()) || NodeTypeEnum.ROOT.equals(node.getType())) {
                childManualNodeIds.add(node.getId());
            }
        }
    }
}
