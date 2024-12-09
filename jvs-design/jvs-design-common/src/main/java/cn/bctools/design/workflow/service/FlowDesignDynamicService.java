package cn.bctools.design.workflow.service;

import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.dto.FlowExtendDto;
import cn.bctools.design.workflow.model.Node;

/**
 * @author zhuxiaokang
 * 动态工作流服务
 */
public interface FlowDesignDynamicService {
    /**
     * 增加节点到设计
     *
     * @param updateDesign TRUE-修改流程设计，FALSE-不修改流程设计（临时保存新加的节点）
     * @param flowTask     流程任务
     * @param flowExtend   流程设计高级配置
     * @param currentNode  当前节点
     * @param newNode      动态新增的下级节点
     */
    void addNode(boolean updateDesign, FlowTask flowTask, FlowExtendDto flowExtend, Node currentNode, Node newNode);
}
