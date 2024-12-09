package cn.bctools.design.workflow.support.function;


import cn.bctools.design.workflow.model.Node;

/**
 * @author zhuxiaokang
 * 工作流支持的各种功能
 */
public abstract class AbstractFunctionHandler<R, T>{

    /**
     * 执行节点支持的功能
     * @param node 节点
     * @param runtimeData 运行时数据
     * @return 返回
     */
     public abstract R invoke(Node node, T runtimeData);
}
