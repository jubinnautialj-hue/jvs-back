package cn.bctools.design.workflow.support.runtime;

import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.entity.enums.ProcessStatusEnum;

/**
 * @author zhuxiaokang
 */
public interface RuntimeTaskNodeService {

    /**
     * 保存当前节点处理结果
     *
     * @param processStatusEnum 节点处理状态
     * @return 返回任务节点信息
     */
    FlowTaskNode saveResult(ProcessStatusEnum processStatusEnum);


}
