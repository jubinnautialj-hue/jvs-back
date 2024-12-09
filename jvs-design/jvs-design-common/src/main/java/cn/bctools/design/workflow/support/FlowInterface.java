package cn.bctools.design.workflow.support;


import cn.bctools.design.workflow.model.enums.NodeTypeEnum;

/**
 * The interface Flow interface.
 *
 * @author zhuxiaokang  工作流流转处理. 所有工作流节点类型都需要实现此接口
 */
public interface FlowInterface {

    /**
     * 节点类型
     *
     * @return the type
     */
    NodeTypeEnum getType();

    /**
     * 流转处理
     */
    void execute();
}
