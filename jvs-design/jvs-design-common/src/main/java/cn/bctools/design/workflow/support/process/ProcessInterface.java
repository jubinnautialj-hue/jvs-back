package cn.bctools.design.workflow.support.process;


import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.enums.FlowNextTypeEnum;

/**
 * @author zhuxiaokang
 * 流转处理。对工作流节点执行结果进行处理
 */
public interface ProcessInterface {

    /**
     * 处理类型
     *
     * @return 处理类型
     */
    FlowNextTypeEnum getType();

    /**
     * 流转处理
     *
     * @param flowResult 节点处理结果
     */
    void execute(FlowResult flowResult);
}
