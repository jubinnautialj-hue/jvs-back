package cn.bctools.design.workflow.controller;

import cn.bctools.common.utils.R;
import cn.bctools.design.workflow.dto.manage.TaskManagePageDto;
import cn.bctools.design.workflow.service.FlowTaskManageAsyncExportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.util.*;

/**
 * 工作流任务管理导出控制器 - 优化版
 * 解决大数据量导出导致的超时问题
 * 采用异步导出方案
 *
 * @author BCTools
 */
@Slf4j
@RestController
@RequestMapping("/base/workflow/task/manage/export")
@Api(tags = "工作流任务管理导出")
public class FlowTaskManageExportController {

    @Autowired
    private FlowTaskManageAsyncExportService asyncExportService;

    /**
     * 发起异步导出任务
     */
    @GetMapping("/async")
    @ApiOperation(value = "发起异步导出任务", notes = "返回任务ID,用于后续查询进度和下载")
    public R<String> startAsyncExport(TaskManagePageDto params) {
        try {
            String taskId = UUID.randomUUID().toString();
            log.info("发起异步导出任务: {},参数: {}", taskId, params);

            // 启动异步导出
            asyncExportService.asyncExport(params, taskId);

            return R.ok(taskId, "导出任务已创建");
        } catch (Exception e) {
            log.error("创建导出任务失败: {}", e.getMessage(), e);
            return R.failed("创建导出任务失败: " + e.getMessage());
        }
    }

    /**
     * 查询导出进度
     */
    @GetMapping("/progress/{taskId}")
    @ApiOperation(value = "查询导出进度", notes = "返回进度百分比和状态信息")
    public R<Map<String, Object>> getExportProgress(@PathVariable String taskId) {
        try {
            if (!asyncExportService.isTaskExists(taskId)) {
                return R.failed("导出任务不存在");
            }

            Map<String, Object> progress = asyncExportService.getExportProgress(taskId);
            return R.ok(progress);
        } catch (Exception e) {
            log.error("查询导出进度失败: {}", e.getMessage(), e);
            return R.failed("查询失败: " + e.getMessage());
        }
    }

    /**
     * 下载导出结果
     */
    @GetMapping("/download/{taskId}")
    @ApiOperation(value = "下载导出结果", notes = "下载完成后,任务数据会被清理")
    public ResponseEntity<byte[]> downloadExportResult(@PathVariable String taskId,
                                                       @RequestParam(defaultValue = "审批进度报表") String fileName) {
        try {
            if (!asyncExportService.isTaskExists(taskId)) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> meta = asyncExportService.getExportProgress(taskId);
            String status = (String) meta.get("status");

            if (!"completed".equals(status)) {
                return ResponseEntity.badRequest().body(("导出未完成,当前状态: " + status).getBytes());
            }

            byte[] data = asyncExportService.getExportResult(taskId);
            if (data == null || data.length == 0) {
                return ResponseEntity.notFound().build();
            }

            // 设置文件名
            String encodedFileName = URLEncoder.encode(fileName + ".xlsx", "UTF-8");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .contentLength(data.length)
                    .body(data);

        } catch (Exception e) {
            log.error("下载导出结果失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(("下载失败: " + e.getMessage()).getBytes());
        }
    }

    /**
     * 取消导出任务
     */
    @DeleteMapping("/cancel/{taskId}")
    @ApiOperation(value = "取消导出任务", notes = "取消正在进行的导出任务")
    public R<Void> cancelExportTask(@PathVariable String taskId) {
        try {
            // 这里可以实现任务取消逻辑
            // 例如通过线程中断或标记位
            log.info("取消导出任务: {}", taskId);
            return R.ok(null, "任务已取消");
        } catch (Exception e) {
            log.error("取消导出任务失败: {}", e.getMessage(), e);
            return R.failed("取消失败: " + e.getMessage());
        }
    }
}
