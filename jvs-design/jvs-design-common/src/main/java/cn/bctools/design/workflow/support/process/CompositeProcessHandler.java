package cn.bctools.design.workflow.support.process;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.design.workflow.dto.FlowReqDto;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.entity.FlowTaskPerson;
import cn.bctools.design.workflow.entity.dto.CourseDto;
import cn.bctools.design.workflow.entity.dto.FlowManualNode;
import cn.bctools.design.workflow.entity.enums.ProcessStatusEnum;
import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.model.enums.NodeTypeGroupEnum;
import cn.bctools.design.workflow.service.FlowTaskPersonService;
import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.enums.FlowNextTypeEnum;
import cn.bctools.design.workflow.support.listener.person.RemoveTaskPersonEvent;
import cn.bctools.design.workflow.support.runtime.RuntimeTaskNodeService;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import cn.bctools.design.workflow.utils.FlowUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 执行结果处理入口
 */
@Slf4j
@Component
@AllArgsConstructor
public class CompositeProcessHandler {

    /**
     * 不需要保存处理结果的节点类型
     */
    private static final List<NodeTypeEnum> NO_SAVE_COURSES_NODE_TYPE = Arrays.asList(NodeTypeEnum.CONDITION, NodeTypeEnum.PARALLEL);

    /**
     * 需要保存处理结果的流转类型
     */
    private static final List<FlowNextTypeEnum> SAVE_COURSES_NEXT_TYPE = Arrays.asList(FlowNextTypeEnum.END, FlowNextTypeEnum.NEXT, FlowNextTypeEnum.BACK);

    private final List<ProcessInterface> circulationInterfaces;
    private final RuntimeTaskNodeService runtimeTaskNodeService;
    private final FlowTaskPersonService flowTaskPersonService;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 执行结果处理
     */
    public void execute(FlowResult flowResult) {
        // 前置处理
        before(flowResult, FlowContextUtil.context().getRuntimeData());

        // 不同类型的执行结果处理
        circulationInterfaces.stream()
                .filter(f -> f.getType().equals(flowResult.getFlowNextTypeEnum()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("不支持的处理类型"))
                .execute(flowResult);
    }

    /**
     * 前置处理
     *
     * @param flowResult 运行时数据
     */
    private void before(FlowResult flowResult, RuntimeData runtimeData) {
        Node currentNode = runtimeData.getCurrentNode();
        String currentNodeId = runtimeData.getNodeId();
        FlowTask flowTask = runtimeData.getFlowTask();
        // 不需要保存处理结果的节点类型，不做后续处理
        if (NO_SAVE_COURSES_NODE_TYPE.contains(currentNode.getType())) {
            return;
        }

        // 保存当前节点执行结果
        log.debug("保存当前节点{}处理结果", runtimeData.getNodeId());
        if (FlowNextTypeEnum.PENDING.equals(flowResult.getFlowNextTypeEnum())) {
            // 保存当前节点结果
            runtimeTaskNodeService.saveResult(ProcessStatusEnum.PENDING);
        }
        // 流转结果处理
        if (SAVE_COURSES_NEXT_TYPE.contains(flowResult.getFlowNextTypeEnum())) {
            // 保存当前节点结果
            FlowTaskNode flowTaskNode = runtimeTaskNodeService.saveResult(ProcessStatusEnum.PROCESSED);
            if (flowTaskNode != null) {
                // 删除当前节点待办人信息
                List<String> removeTaskPersonIds = flowTaskPersonService
                        .list(Wrappers.<FlowTaskPerson>lambdaQuery().eq(FlowTaskPerson::getFlowTaskId, runtimeData.getFlowTask().getId()).eq(FlowTaskPerson::getNodeId, runtimeData.getCurrentNode().getId()))
                        .stream().map(FlowTaskPerson::getId).collect(Collectors.toList());
                applicationEventPublisher.publishEvent(new RemoveTaskPersonEvent(this, removeTaskPersonIds));
                // 变更任务表最新信息
                LinkedList<CourseDto> courseDtos = Optional.ofNullable(flowTask.getCourses()).orElse(new LinkedList<>());
                courseDtos.add(flowTaskNode.getCourse());
                flowTask.setCourses(courseDtos);
            }

            // 当前节点加入已处理人工节点信息
            FlowReqDto flowDto = Optional.ofNullable(runtimeData.getFlowDto()).orElse(new FlowReqDto());
            NodeOperationTypeEnum nodeOperationTyp = flowDto.getNodeOperationType();
            // 回退操作不将当前节点加入已处理人工节点信息
            if (Boolean.FALSE.equals(NodeOperationTypeEnum.BACK.equals(nodeOperationTyp))) {
                addManualNode(flowTask, currentNodeId);
                runtimeData.setFlowTask(flowTask);
                FlowContextUtil.refreshContext(runtimeData);
            }
        }
    }

    /**
     * 当前节点加入已处理人工节点信息
     *
     * @param flowTask      工作流任务
     * @param currentNodeId 当前节点id
     */
    private void addManualNode(FlowTask flowTask, String currentNodeId) {
        // 获取当前节点
        Node currentNode = StringUtils.isBlank(currentNodeId) ? FlowUtil.getRootNode(flowTask.getDesignBody()) : FlowUtil.findNode(currentNodeId);
        // 当前节点加入已处理人工节点
        // 人工节点 || 开始节点 都可以加入
        boolean add = ObjectUtil.isNotNull(currentNode) && (NodeTypeGroupEnum.MANUAL.equals(currentNode.getType().getGroup()) ||
                NodeTypeEnum.ROOT.equals(currentNode.getType())
        );
        if (add) {
            LinkedList<FlowManualNode> manualNodes = Optional.ofNullable(flowTask.getFlowManualNodes()).orElse(new LinkedList<>());
            manualNodes.addLast(new FlowManualNode().setId(currentNode.getId()).setName(currentNode.getName()));
            flowTask.setFlowManualNodes(manualNodes);
        }
    }

}
