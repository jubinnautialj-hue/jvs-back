package cn.bctools.design.workflow.support.impl;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.dto.ApproveResultDto;
import cn.bctools.design.workflow.entity.dto.CourseDto;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.support.ExecuteTask;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.RuntimeService;
import cn.bctools.design.workflow.support.StartTask;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedList;

/**
 * @author zhuxiaokang
 */
@Service
@AllArgsConstructor
public class RuntimeServiceImpl implements RuntimeService {

    private final CompositeFlowHandler compositeFlowHandler;

    @Override
    public RuntimeData start(StartTask startTask) {
        // 1. 准备启动流程
        RuntimeData runtimeData = new RuntimeData();
        BeanUtils.copyProperties(startTask, runtimeData);
        // 初始化工作流运行时上下文
        FlowContextUtil.context().init();
        // 设置当前上下文为启动工作流
        FlowContextUtil.context().startFlow();
        // 封装开始节点进度
        FlowTask flowTask = runtimeData.getFlowTask();
        String startTime = LocalDateTimeUtil.format(flowTask.getCreateTime(), DatePattern.NORM_DATETIME_PATTERN);
        CourseDto courseDto = new CourseDto()
                .setNodeName("开始")
                .setTime(startTime)
                .setDataVersion(runtimeData.getDataVersion())
                .setApproveResultDtos(Collections.singletonList(new ApproveResultDto().setUserName(flowTask.getCreateBy()).setUserId(flowTask.getCreateById()).setTime(startTime)))
                .setNodeType(NodeTypeEnum.ROOT);
        LinkedList<CourseDto> courses = new LinkedList<>();
        courses.add(courseDto);
        flowTask.setCourses(courses);

        //2. 启动流程
        compositeFlowHandler.execute(runtimeData);
        return runtimeData;
    }

    @Override
    public RuntimeData execute(ExecuteTask executeTask) {
        // 1. 准备执行流程
        RuntimeData runtimeData = new RuntimeData();
        BeanUtils.copyProperties(executeTask, runtimeData);
        
        if (ObjectNull.isNotNull(executeTask.getFlowTaskNode()) && ObjectNull.isNotNull(executeTask.getFlowTaskNode().getCourse())) {
            runtimeData.setCurrentNodeMode(executeTask.getFlowTaskNode().getCourse().getMode());
        }

        // 初始化工作流运行时上下文
        FlowContextUtil.context().init();
        // 2. 执行流程
        compositeFlowHandler.execute(runtimeData);

        return runtimeData;
    }
}
