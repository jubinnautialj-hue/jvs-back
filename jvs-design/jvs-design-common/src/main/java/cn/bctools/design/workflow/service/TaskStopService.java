package cn.bctools.design.workflow.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.workflow.dto.BatchStopTaskResDto;
import cn.bctools.design.workflow.dto.StopTaskReqDto;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.enums.FlowTaskStatusEnum;

/**
 * @author zhuxiaokang
 * 工作流任务结束服务
 */
public interface TaskStopService {

    /**
     * 发起人撤回流程任务
     *
     * @param userDto     用户
     * @param taskId      流程任务id
     * @param stopTaskDto 参数
     * @return 任务对象
     */
    FlowTask withdrawTask(UserDto userDto, String taskId, StopTaskReqDto stopTaskDto);

    /**
     * 终止流程任务
     *
     * @param userDto     用户
     * @param flowTask    流程任务
     * @param stopTaskDto 参数
     * @return 任务对象
     */
    FlowTask terminationTask(UserDto userDto, FlowTask flowTask, StopTaskReqDto stopTaskDto);

    /**
     * 批量终止流程任务
     *
     * @param userDto     用户
     * @param taskIds     流程任务ID列表
     * @param stopTaskDto 参数
     * @return 终止结果（成功数量、失败数量、失败详情）
     */
    BatchStopTaskResDto batchTerminationTask(UserDto userDto, java.util.List<String> taskIds, StopTaskReqDto stopTaskDto);

    /**
     * 任务结束
     *
     * @param task   工作流任务
     * @param status 工作流任务状态
     * @param reason 终止任务原因
     */
    void endTask(FlowTask task, FlowTaskStatusEnum status, String reason);

}
