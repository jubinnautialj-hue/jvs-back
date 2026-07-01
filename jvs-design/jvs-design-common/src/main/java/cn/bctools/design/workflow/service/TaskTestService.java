package cn.bctools.design.workflow.service;

import cn.bctools.design.workflow.dto.SaveFlowReqDesign;
import cn.bctools.design.workflow.dto.testflow.FlowTestPrepareDto;
import cn.bctools.design.workflow.dto.testflow.FlowTestReqDto;
import cn.bctools.design.workflow.dto.testflow.StartFlowTestDto;
import cn.bctools.design.workflow.dto.testflow.StartFlowTestResDto;

/**
 * @author zhuxiaokang
 * 测试工作流任务流转服务
 */
public interface TaskTestService {

    /**
     * 发起工作流测试准备
     *
     * @param dto 保存工作流设计入参
     * @return 发起工作流测试准备数据
     */
    FlowTestPrepareDto prepare(SaveFlowReqDesign dto);

    /**
     * 启动测试工作流
     *
     * @param dto 入参
     * @return 工作流任务id
     */
    StartFlowTestResDto startTask(StartFlowTestDto dto);

    /**
     * 执行测试工作流
     *
     * @param flowDto 审批入参
     */
    void execute(FlowTestReqDto flowDto);

    /**
     * 重新发起测试工作流任务
     *
     * @param dto 入参
     * @return 返回工作流信息
     */
    StartFlowTestResDto restartTask(StartFlowTestDto dto);
}
