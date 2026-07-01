package cn.bctools.design.workflow.support.function.impl;

import cn.bctools.design.workflow.dto.FlowReqDto;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.entity.FlowTaskPerson;
import cn.bctools.design.workflow.enums.BackTaskResubmitEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.service.FlowTaskNodeService;
import cn.bctools.design.workflow.service.FlowTaskPersonService;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.function.AbstractFunctionHandler;
import cn.bctools.design.workflow.support.function.dto.BackResubmitDto;
import cn.bctools.design.workflow.utils.FlowUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author zhuxiaokang
 * 回退后重新发起处理
 */
@Slf4j
@Component
@AllArgsConstructor
public class BackResubmitFunction extends AbstractFunctionHandler<BackResubmitDto, RuntimeData> {

    private final FlowTaskNodeService flowTaskNodeService;
    private final FlowTaskPersonService flowTaskPersonService;

    /**
     * 如是回退后重新提交的，需要根据"被回退的数据重新提交配置"确定下一步流转的节点
     * <p>
     *     - 逆序遍历集合，判断当前审批操作是否是审批回退后的第一次提交
     *     - 若是审批回退后的第一次提交：根据回退节点的回退配置，返回下一步流转的节点
     *     - 若不是审批回退后的第一次提交： 沿用节点处理结果flowResult中返回的下一步流转节点
     *
     * @param node 节点
     * @param runtimeData 运行时数据
     * @return 下一步流转的节点以及回退相关配置
     */
    @Override
    public BackResubmitDto invoke(Node node, RuntimeData runtimeData) {
        FlowTask flowTask = runtimeData.getFlowTask();
        FlowReqDto flowReq = Optional.ofNullable(runtimeData.getFlowDto()).orElseGet(FlowReqDto::new);
        BackResubmitDto resubmitDto = FlowUtil.parseBackTaskResubmitNextNode(flowReq.getNodeId(), flowTask);
        // 恢复挂起的待办任务
        restoreTask(flowTask, resubmitDto.getBackTaskResubmit());
        return resubmitDto;
    }

    /**
     * 恢复挂起的待办任务
     *
     * @param flowTask 工作流任务
     * @param resubmit 发起回退操作的节点的回退规则配置
     */
    private void restoreTask(FlowTask flowTask, BackTaskResubmitEnum resubmit) {
        // 回退后重新发起规则为直达当前（发起回退的）节点，挂起待办相关数据
        if (BackTaskResubmitEnum.DIRECT_CURRENT_NODE.equals(resubmit)) {
            // 挂起节点
            flowTaskNodeService.update(Wrappers.<FlowTaskNode>lambdaUpdate()
                    .set(FlowTaskNode::getHang, Boolean.FALSE)
                    .eq(FlowTaskNode::getFlowTaskId, flowTask.getId()));
            // 挂起待办人
            flowTaskPersonService.update(Wrappers.<FlowTaskPerson>lambdaUpdate()
                    .set(FlowTaskPerson::getHang, Boolean.FALSE)
                    .eq(FlowTaskPerson::getFlowTaskId, flowTask.getId()));
        }
    }
}
