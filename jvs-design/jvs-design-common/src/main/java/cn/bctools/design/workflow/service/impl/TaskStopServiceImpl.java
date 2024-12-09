package cn.bctools.design.workflow.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.dto.StopTaskReqDto;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.entity.dto.ApproveResultDto;
import cn.bctools.design.workflow.entity.dto.CourseDto;
import cn.bctools.design.workflow.entity.dto.FlowExtendDto;
import cn.bctools.design.workflow.entity.enums.FlowTaskStatusEnum;
import cn.bctools.design.workflow.entity.enums.TerminatedTypeEnum;
import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import cn.bctools.design.workflow.service.FlowDesignService;
import cn.bctools.design.workflow.service.FlowTaskNodeService;
import cn.bctools.design.workflow.service.FlowTaskService;
import cn.bctools.design.workflow.service.TaskStopService;
import cn.bctools.design.workflow.support.listener.asynctask.AsyncTaskDynamicDataEvent;
import cn.bctools.design.workflow.support.listener.taskend.TaskEndEvent;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 结束任务服务
 */
@Service
@AllArgsConstructor
public class TaskStopServiceImpl implements TaskStopService {

    private final FlowTaskService flowTaskService;
    private final FlowDesignService flowDesignService;
    private final FlowTaskNodeService flowTaskNodeService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public FlowTask withdrawTask(UserDto userDto, String taskId, StopTaskReqDto stopTaskDto) {
        // 查询工作流任务信息
        FlowTask flowTask = Optional.ofNullable(flowTaskService.getOne(Wrappers.<FlowTask>lambdaQuery()
                .eq(FlowTask::getId, taskId)
                .eq(FlowTask::getCreateById, userDto.getId())
        )).orElseThrow(() -> new BusinessException("只能取消自己发起的任务"));
        if (Boolean.FALSE.equals(FlowTaskStatusEnum.PENDING.equals(flowTask.getTaskStatus()))) {
            throw new BusinessException("任务已结束");
        }
        // 查询工作流设计，判断是否允许发起人终止流程（若流程设计已删除，则默认不可人为终止）
        FlowExtendDto extend = flowDesignService.getFlowExtend(flowTask.getFlowDesignId());
        if (Boolean.FALSE.equals(extend.getEnableCancel())) {
            throw new BusinessException("不允许发起人终止流程");
        }
        // 结束任务
        stopTask(userDto, flowTask, stopTaskDto, TerminatedTypeEnum.WITHDRAW, extend);
        // 发布事件：同步流程信息到业务数据
        applicationEventPublisher.publishEvent(new AsyncTaskDynamicDataEvent(this, flowTask));
        return flowTask;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public FlowTask terminationTask(UserDto userDto, FlowTask flowTask, StopTaskReqDto stopTaskDto) {
        if (Boolean.FALSE.equals(FlowTaskStatusEnum.PENDING.equals(flowTask.getTaskStatus()))) {
            throw new BusinessException("任务已结束");
        }
        FlowExtendDto extend = flowDesignService.getFlowExtend(flowTask.getFlowDesignId());
        // 结束任务
        stopTask(userDto, flowTask, stopTaskDto, TerminatedTypeEnum.TERMINATION, extend);
        return flowTask;
    }

    /**
     * 结束任务
     *
     * @param userDto        用户
     * @param flowTask       任务
     * @param stopTaskDto    参数
     * @param terminatedType 终止任务类型
     * @param extend         流程设计扩展配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void stopTask(UserDto userDto, FlowTask flowTask, StopTaskReqDto stopTaskDto, TerminatedTypeEnum terminatedType, FlowExtendDto extend) {
        LinkedList<CourseDto> courseDtos = Optional.ofNullable(flowTask.getCourses()).orElse(new LinkedList<>());
        // 得到未审批结束的节点审批进度
        List<CourseDto> currentNodeCourses = flowTaskNodeService.getCurrentNodesByTaskId(flowTask.getId()).stream()
                .map(FlowTaskNode::getCourse)
                .filter(courseDto -> ObjectNull.isNotNull(courseDto.getApproveResultDtos()))
                .collect(Collectors.toList());
        if (ObjectNull.isNotNull(currentNodeCourses)) {
            courseDtos.addAll(currentNodeCourses);
        }
        // 保存处理结果
        String time = LocalDateTimeUtil.format(flowTask.getUpdateTime(), DatePattern.NORM_DATETIME_PATTERN);
        CourseDto courseDto = new CourseDto()
                .setTerminated(Boolean.TRUE)
                .setTerminatedType(terminatedType)
                .setTerminatedReason(stopTaskDto.getReason())
                .setApproveResultDtos(Collections.singletonList(new ApproveResultDto()
                        .setUserId(userDto.getId())
                        .setUserName(userDto.getRealName())
                        .setTime(time)
                        .setNodeOperationTypeEnum(NodeOperationTypeEnum.TERMINATED)
                        .setOver(Boolean.TRUE)))
                .setTime(time);
        courseDtos.add(courseDto);
        flowTask.setCourses(courseDtos);
        flowTaskService.updateById(flowTask);
        // 终止任务
        endTask(flowTask, FlowTaskStatusEnum.TERMINATED, stopTaskDto.getReason());
        // 发布事件：任务结束
        applicationEventPublisher.publishEvent(new TaskEndEvent(this, flowTask, extend));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void endTask(FlowTask task, FlowTaskStatusEnum status, String reason) {
        // 修改任务状态
        task.setTaskStatus(status);
        task.setStopReason(reason);
        flowTaskService.updateById(task);
        // 清除工作流执行过程数据
        flowTaskService.cleanTaskExecutiveProcess(task.getId());
    }
}
