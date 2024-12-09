package cn.bctools.design.workflow.controller;

import cn.bctools.common.utils.R;
import cn.bctools.design.workflow.dto.SaveFlowReqDesign;
import cn.bctools.design.workflow.dto.testflow.*;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.service.FlowTaskService;
import cn.bctools.design.workflow.service.TaskTestService;
import cn.bctools.log.annotation.Log;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Api(tags = "[workflow]测试工作流")
@AllArgsConstructor
@RestController
@RequestMapping("/base/workflow/task/test")
public class FlowTaskTestController {

    private final TaskTestService taskTestService;
    private final FlowTaskService flowTaskService;

    @Log
    @ApiOperation(value = "测试准备", notes = "准备测试相关数据")
    @PostMapping("/prepare")
    public R<FlowTestPrepareDto> prepare(@Validated @RequestBody SaveFlowReqDesign dto) {
        return R.ok(taskTestService.prepare(dto));
    }

    @Log
    @ApiOperation("启动工作流")
    @PostMapping("/start")
    public R<StartFlowTestResDto> start(@Validated @RequestBody StartFlowTestDto dto) {
        return R.ok(taskTestService.startTask(dto));
    }

    @Log
    @ApiOperation("执行工作流任务")
    @PostMapping("/execute")
    public R<FlowTestResDto> execute(@Validated @RequestBody FlowTestReqDto dto) {
        // 任务流转
        taskTestService.execute(dto);
        // 查询工作流任务
        FlowTask flowTask = flowTaskService.getById(dto.getId());
        return R.ok(new FlowTestResDto().setFlowManualNodes(flowTask.getFlowManualNodes()));
    }

    @Log
    @ApiOperation(value = "重新发起工作流任务")
    @PostMapping("/restart")
    public R<StartFlowTestResDto> restart(@Validated @RequestBody StartFlowTestDto dto) {
        return R.ok(taskTestService.restartTask(dto));
    }

}
