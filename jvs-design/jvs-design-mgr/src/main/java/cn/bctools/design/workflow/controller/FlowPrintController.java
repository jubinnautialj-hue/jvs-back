package cn.bctools.design.workflow.controller;

import cn.bctools.common.utils.R;
import cn.bctools.design.workflow.dto.progress.ProgressPrintResDto;
import cn.bctools.design.workflow.service.FlowTaskService;
import cn.bctools.log.annotation.Log;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author zhuxiaokang
 */

@Api(tags = "[workflow]工作流打印")
@RestController
@RequestMapping("/base/workflow/print")
@AllArgsConstructor
public class FlowPrintController {

    private final FlowTaskService flowTaskService;

    @Log
    @ApiOperation("获取任务进度数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "任务id", required = true)
    })
    @GetMapping("/progress/{id}")
    public R<List<ProgressPrintResDto>> getProgressPrint(@PathVariable String id) {
        return R.ok(flowTaskService.getProgressPrint(id));
    }
}
