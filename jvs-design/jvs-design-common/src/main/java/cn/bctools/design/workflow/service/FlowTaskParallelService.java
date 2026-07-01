package cn.bctools.design.workflow.service;

import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskParallel;
import cn.bctools.design.workflow.entity.dto.ParallelBranchDto;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author zhuxiaokang
 * 工作流并行任务 服务类
 */
public interface FlowTaskParallelService extends IService<FlowTaskParallel> {

    /**
     * 初始化并行任务记录
     *
     * @param flowTask       任务
     * @param parallelNodeId 并行节点id
     * @param branchs        并行分支完成状态
     */
    void init(FlowTask flowTask, String parallelNodeId, List<ParallelBranchDto> branchs);

    /**
     * 修改已完成分支数
     *
     * @param taskId         任务id
     * @param parallelNodeId 并行节点id
     * @param branchNodeId   并行分支id
     * @return TRUE-并行结束，FALSE-并行未结束
     */
    Boolean checkCompleted(String taskId, String parallelNodeId, String branchNodeId);

    /**
     * 删除工作流任务并行任务记录
     *
     * @param flowTaskId 任务id
     */
    void removeTaskAll(String flowTaskId);

}
