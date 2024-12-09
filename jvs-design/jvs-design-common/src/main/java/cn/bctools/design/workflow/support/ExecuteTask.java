package cn.bctools.design.workflow.support;

import cn.bctools.design.workflow.dto.FlowReqDto;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.model.Node;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 * 执行流程任务入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class ExecuteTask extends BaseTask{
    /**
     * 正在处理的节点id
     */
    private String nodeId;

    /**
     * 当前处理的节点
     */
    private Node currentNode;

    /**
     * 工作流处理操作信息入参
     */
    private FlowReqDto flowDto;

    /**
     * 当前处理的工作流任务节点信息
     */
    private FlowTaskNode flowTaskNode;


}
