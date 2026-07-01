package cn.bctools.design.workflow.controller;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.workflow.dto.AppendApprovalPointResDto;
import cn.bctools.design.workflow.dto.ApproveNodeDto;
import cn.bctools.design.workflow.dto.ApproveNodesDto;
import cn.bctools.design.workflow.dto.CheckDynamicNodeDto;
import cn.bctools.design.workflow.entity.FlowDesign;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.entity.dto.FlowExtendDto;
import cn.bctools.design.workflow.entity.enums.FlowDesignVersionStatusEnum;
import cn.bctools.design.workflow.entity.enums.FlowTaskNodeApprovalTypeEnum;
import cn.bctools.design.workflow.entity.enums.FlowTaskStatusEnum;
import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.AppendApprovalPointEnum;
import cn.bctools.design.workflow.model.enums.NodePropertiesTypeEnum;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.model.properties.FlowButton;
import cn.bctools.design.workflow.service.FlowDesignService;
import cn.bctools.design.workflow.service.FlowTaskNodeService;
import cn.bctools.design.workflow.service.FlowTaskService;
import cn.bctools.design.workflow.support.function.dto.BackResubmitDto;
import cn.bctools.design.workflow.utils.FlowDynamicUtil;
import cn.bctools.design.workflow.utils.FlowPathUtil;
import cn.bctools.design.workflow.utils.FlowUtil;
import cn.bctools.log.annotation.Log;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Api(tags = "[workflow]工作流任务节点")
@AllArgsConstructor
@RestController
@RequestMapping("/base/workflow/task/node")
public class FlowTaskNodeController {

    /**
     * 分支节点
     */
    private static final List<NodeTypeEnum> BRANCH_TYPE = Arrays.asList(NodeTypeEnum.PARALLEL,NodeTypeEnum.PB, NodeTypeEnum.CONDITION, NodeTypeEnum.TJ);

    private final FlowTaskNodeService flowTaskNodeService;
    private final FlowTaskService flowTaskService;
    private final FlowDesignService flowDesignService;

    @Log
    @ApiOperation("获取指定任务节点的加签方式")
    @GetMapping("/append_type/{taskId}/{nodeId}")
    public R<List<AppendApprovalPointResDto>> getAppendType(@PathVariable String taskId, @PathVariable String nodeId) {
        List<AppendApprovalPointResDto> appendApprovalPoints = new ArrayList<>();
        FlowTaskNode flowTaskNode = flowTaskNodeService.getCurrentPendingNode(taskId, nodeId);
        if (ObjectNull.isNull(flowTaskNode)) {
            return R.ok(Collections.emptyList());
        }

        // 当前审批类型为“加签审批“，可选的加签方式默认为”前加签“
        if (FlowTaskNodeApprovalTypeEnum.APPEND_APPROVAL.equals(flowTaskNode.getApprovalType())) {
            appendApprovalPoints.add(new AppendApprovalPointResDto().setValue(AppendApprovalPointEnum.BEFORE.getValue()).setName(AppendApprovalPointEnum.BEFORE.getDesc()));
        } else {
            // 非”加签审批“，根据节点的审批配置获取加签方式
            FlowTask flowTask = flowTaskService.getTaskById(taskId);
            Node node = Optional.ofNullable(FlowUtil.findNode(flowTask.getDesignBody(), nodeId)).orElseThrow(() -> new BusinessException("获取节点信息失败"));
            List<FlowButton> btns = node.getProps().getBtn();
            if (CollectionUtils.isEmpty(btns)) {
                return R.ok(Collections.emptyList());
            }
            // 未启用加签按钮，不返回加签方式
            if (btns.stream().noneMatch(btn -> btn.getOperation().equals(NodeOperationTypeEnum.APPEND) && Boolean.TRUE.equals(btn.getEnable()))) {
                return R.ok(Collections.emptyList());
            }
            // 已启用加签按钮，返回加签方式
            List<AppendApprovalPointEnum> points = node.getProps().getAppendApproval().getPoint();
            if (CollectionUtils.isNotEmpty(points)) {
                points.forEach(point -> {
                    appendApprovalPoints.add(new AppendApprovalPointResDto().setValue(point.getValue()).setName(point.getDesc()));
                });
            }
        }
        return R.ok(appendApprovalPoints);
    }

    @Log
    @ApiOperation(value = "获取任务指定审批节点的下级审批节点集合")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nextOne", value = "true或null-下一个审批节点, false-所有下级审批节点", required = true)
    })
    @GetMapping("/next/{taskId}/{nodeId}")
    public R<List<ApproveNodeDto>> getNextApproveNode(@PathVariable String taskId, @PathVariable String nodeId, Boolean nextOne) {
        FlowTaskNode flowTaskNode = flowTaskNodeService.getCurrentPendingNode(taskId, nodeId);
        if (ObjectNull.isNull(flowTaskNode)) {
            return R.ok(Collections.emptyList());
        }
        FlowTask flowTask = flowTaskService.getTaskById(taskId);
        String designBody = flowTask.getDesignBody();
        Node currentNode = FlowUtil.findNode(designBody, nodeId);

        FlowDesign flowDesign = Optional.ofNullable(flowDesignService.getOne(Wrappers.<FlowDesign>lambdaQuery().eq(FlowDesign::getId, flowTask.getFlowDesignId()))).orElseGet(FlowDesign::new);
        FlowExtendDto flowExtend = flowDesign.getExtend();

        // 若当前节点是回退后的节点，且指定了下一步流转的节点，则直接返回该节点
        BackResubmitDto backResubmit = FlowUtil.parseBackTaskResubmitNextNode(currentNode.getId(), flowTask);
        if (backResubmit.getWhetherResubmit() && ObjectNull.isNotNull(backResubmit.getNextNode())) {
            Node nextNode = backResubmit.getNextNode();
            // 节点是否允许动态选择审批人
            boolean canDynamicApprover = FlowUtil.getNodeCanDynamicApprover(flowExtend, nextNode);
            return R.ok(Collections.singletonList(BeanCopyUtil.copy(nextNode, ApproveNodeDto.class).setCanDynamicApprover(canDynamicApprover)));
        }

        // 获取当前节点下所有审批节点
        List<Node> nextNodeAll = FlowUtil.getOrderNodes(designBody, currentNode, Collections.singletonList(NodeTypeEnum.SP));
        if (CollectionUtils.isEmpty(nextNodeAll)) {
            return R.ok();
        }
        // 返回所有下级审批节点
        if (Boolean.FALSE.equals(nextOne)) {
            return R.ok(nextNodeAll.stream()
                    .map(node -> {
                        Node n = FlowUtil.findNode(node.getId()).setNode(null);
                        // 节点是否允许动态选择审批人
                        boolean canDynamicApprover = FlowUtil.getNodeCanDynamicApprover(flowExtend, n);
                        return BeanCopyUtil.copy(n, ApproveNodeDto.class).setCanDynamicApprover(canDynamicApprover);
                    })
                    .collect(Collectors.toList()));
        }

        // 返回下一个审批节点
        // 下一个审批节点和当前审批节点的关系必须是线性的。 否则不返回（如：当前节点到下一个审批节点的路径中包含条件分支、并行分支）
        List<List<Node>> nodePaths = FlowPathUtil.getNodePaths(designBody, currentNode);
        // 下一个审批节点
        Node nextOneApproveNode = null;
        for (List<Node> nodePath : nodePaths) {
            // 当前路径的下一个审批节点
            Node approveNode = null;
            // true-继续下一个路径筛选，false-结束筛选
            boolean whetherNext = Boolean.TRUE;
            for (Node node : nodePath) {
                if (ObjectNull.isNull(approveNode) && BRANCH_TYPE.contains(node.getType())) {
                    whetherNext = Boolean.FALSE;
                    break;
                }
                if (NodeTypeEnum.SP.equals(node.getType())) {
                    approveNode = node;
                    break;
                }
            }
            if (Boolean.FALSE.equals(whetherNext) || ObjectNull.isNull(approveNode)) {
                break;
            }
            nextOneApproveNode = approveNode;
            break;
        }
        if (ObjectNull.isNull(nextOneApproveNode)) {
            return R.ok();
        }
        Node nextNode = FlowUtil.findNode(nextOneApproveNode.getId()).setNode(null);
        // 节点是否允许动态选择审批人
        boolean canDynamicApprover = FlowUtil.getNodeCanDynamicApprover(flowExtend, nextNode);
        return R.ok(Collections.singletonList(BeanCopyUtil.copy(nextNode, ApproveNodeDto.class).setCanDynamicApprover(canDynamicApprover)));
    }


    @Log
    @ApiOperation(value = "是否允许在当前节点后动态增加节点")
    @GetMapping("/dynamic/node/{taskId}/{nodeId}")
    public R<CheckDynamicNodeDto> checkDynamicNode(@PathVariable String taskId, @PathVariable String nodeId) {
        CheckDynamicNodeDto checkDynamicNodeRes = new CheckDynamicNodeDto();
        checkDynamicNodeRes.setCanDynamicAddNode(Boolean.FALSE);
        FlowTaskNode flowTaskNode = flowTaskNodeService.getCurrentPendingNode(taskId, nodeId);
        if (ObjectNull.isNull(flowTaskNode)) {
            return R.ok(checkDynamicNodeRes);
        }
        FlowTask flowTask = flowTaskService.getTaskById(taskId);
        FlowExtendDto flowExtend = flowDesignService.getFlowExtend(flowTask.getFlowDesignId());
        // 校验是否可以动态增加流程节点
        String designBody = flowTask.getDesignBody();
        checkDynamicNodeRes.setCanDynamicAddNode(FlowDynamicUtil.checkDynamicNode(designBody, flowExtend, FlowUtil.findNode(designBody, nodeId)));
        // 返回当前节点已增加的节点
        checkDynamicNodeRes.setNode(JSON.parseObject(flowTaskNode.getTempNewNode(), Node.class));
        return R.ok(checkDynamicNodeRes);
    }


    @Log
    @ApiOperation(value = "获取工作流审批节点设计集合")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "工作流任务id", required = true)
    })
    @GetMapping("/approve/nodes/{designId}")
    public R<ApproveNodesDto> getSpNodeList(@PathVariable String designId, String taskId) {
        ApproveNodesDto dto = new ApproveNodesDto();
        // 工作流设计
        String design;
        FlowDesign flowDesign = Optional.ofNullable(flowDesignService.getOne(Wrappers.<FlowDesign>lambdaQuery().eq(FlowDesign::getId, designId))).orElseGet(FlowDesign::new);
        // 没有工作流任务，则使用工作流设计
        if (StringUtils.isBlank(taskId)) {
            if (Boolean.FALSE.equals(flowDesign.getPublished())) {
                return R.ok(dto);
            }
            design = flowDesignService.getDesignVersion(flowDesign, FlowDesignVersionStatusEnum.USE).getDesignBody();
        } else {
            // 有工作流任务，则使用工作流任务绑定的设计
            FlowTask flowTask = flowTaskService.getTaskById(taskId);
            if (Boolean.FALSE.equals(designId.equals(flowTask.getFlowDesignId()))) {
                throw new BusinessException("工作流设计id与任务的设计id不同");
            }
            design = flowTask.getDesignBody();
        }
        // 得到审批节点
        List<Node> nodes = FlowUtil.getOrderNodes(design, Collections.singletonList(NodeTypeEnum.SP));
        if (ObjectNull.isNull(nodes)) {
            // 没有审批节点，校验是否可以动态增加节点
            dto.setCanDynamicAddNode(flowDesign.getExtend().getEnableDynamicNode());
        }

        // 得到抄送节点
        List<Node> csNodes = FlowUtil.getOrderNodes(design, Collections.singletonList(NodeTypeEnum.CS))
                .stream()
                // 只返回“发起人自选”类型的节点
                .filter(csNode -> ObjectNull.isNotNull(csNode.getProps().getType()) && NodePropertiesTypeEnum.SELF_SELECT.equals(csNode.getProps().getType()))
                .collect(Collectors.toList());
        if (ObjectNull.isNotNull(csNodes)) {
            nodes.addAll(csNodes);
        }

        dto.setNodes(nodes.stream()
                .map(node -> {
                    Node n = FlowUtil.findNode(node.getId()).setNode(null);
                    // 节点是否允许动态选择审批人
                    boolean canDynamicApprover = FlowUtil.getNodeCanDynamicApprover(flowDesign.getExtend(), n);
                    return BeanCopyUtil.copy(n, ApproveNodeDto.class)
                            .setCanDynamicApprover(canDynamicApprover);
                })
                .collect(Collectors.toList()));

        return R.ok(dto);
    }


    @Log
    @ApiOperation(value = "获取工作流任务待审批节点集合", notes = "通常只有一个待审批节点，有并行任务时,可能有多个待审批节点")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "工作流任务id", required = true)
    })
    @GetMapping("/under/approval/nodes/{taskId}")
    public R<List<Node>> getSpNodeList(@PathVariable String taskId) {
        FlowTask flowTask = flowTaskService.getTaskById(taskId);
        String flowDesignBody = flowTask.getDesignBody();
        if (Boolean.FALSE.equals(FlowTaskStatusEnum.PENDING.equals(flowTask.getTaskStatus()))) {
            return R.ok();
        }
        List<String> taskNodeIds = flowTaskNodeService.getCurrentNodesByTaskId(taskId).stream().map(FlowTaskNode::getNodeId).collect(Collectors.toList());
        if (ObjectNull.isNull(taskNodeIds)) {
            return R.ok();
        }
        List<Node> underApprovalNodes = FlowUtil.getManualNodes(flowDesignBody)
                .stream()
                .filter(node -> taskNodeIds.contains(node.getId()))
                .map(node ->
                        new Node()
                                .setId(node.getId())
                                .setName(node.getName())
                )
                .collect(Collectors.toList());
        return R.ok(underApprovalNodes);
    }
}
