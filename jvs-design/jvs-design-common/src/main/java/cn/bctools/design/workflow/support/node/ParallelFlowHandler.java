package cn.bctools.design.workflow.support.node;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.entity.dto.ParallelBranchDto;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.Parallel;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.service.FlowTaskParallelService;
import cn.bctools.design.workflow.support.impl.AbstractFlowHandler;
import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.enums.FlowNextTypeEnum;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 并行节点处理
 */
@Component
@AllArgsConstructor
public class ParallelFlowHandler extends AbstractFlowHandler {

    private final FlowTaskParallelService flowTaskParallelService;

    @Override
    public NodeTypeEnum getType() {
        return NodeTypeEnum.PARALLEL;
    }

    @Override
    protected void handle(Node node, RuntimeData runtimeData) {
        // 得到有效的并行分支（有执行节点的并行分支）
        List<Parallel> effectiveParallels = node.getParallels().stream()
                .filter(branch -> {
                    Node nextNode = branch.getNode();
                    return nextNode != null && StringUtils.isNotBlank(nextNode.getId());
                }).collect(Collectors.toList());
        // 无有效的并行分支，则获取并行节点的下级节点作为下一步流转节点
        if (CollectionUtils.isEmpty(effectiveParallels)) {
            FlowContextUtil.refreshContext(new FlowResult().setNode(node.getNode()).setFlowNextTypeEnum(FlowNextTypeEnum.NEXT));
        }

        // 并行节点处理
        // 初始化并行任务进度信息
        List<ParallelBranchDto> branchs = effectiveParallels.stream().map(branch -> new ParallelBranchDto().setBranchId(branch.getId()).setComplete(Boolean.FALSE)).collect(Collectors.toList());
        flowTaskParallelService.init(runtimeData.getFlowTask(), node.getId(), branchs);

        // 流转任务
        List<FlowResult> parallelNodes = new ArrayList<>();
        effectiveParallels.forEach(branch -> {
            // 执行流程
            FlowResult flowResult = new FlowResult()
                    .setNode(branch.getNode())
                    .setFlowNextTypeEnum(FlowNextTypeEnum.NEXT);
            parallelNodes.add(flowResult);
        });
        if (ObjectNull.isNotNull(parallelNodes)) {
            FlowContextUtil.refreshExistsParallel(Boolean.TRUE);
        }
        FlowContextUtil.refreshContext(parallelNodes);
    }


}
