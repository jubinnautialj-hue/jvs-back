package cn.bctools.design.workflow.service;

import cn.bctools.design.workflow.dto.manage.TaskManagePageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 异步导出服务类 - 解决大数据量导出性能问题
 *
 * @author BCTools
 */
@Slf4j
@Service
public class AsyncExportService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 异步导出任务
     * 注意:具体的导出逻辑需要在FlowTaskManageController中的exportTaskManage方法实现
     * 此异步服务仅提供异步框架支持
     *
     * @param params 导出参数
     * @param taskId 任务ID
     */
    @Async("exportExecutor")
    public void asyncExport(TaskManagePageDto params, String taskId) {
        log.info("开始异步导出任务: {}, 参数: {}", taskId, params);

        // 更新进度 - 开始
        updateProgress(taskId, 0, "开始导出");

        try {
            // TODO: 由于AsyncExportService在common模块,无法直接访问mgr模块的UseComponent等类
            // 需要在FlowTaskManageController中实现完整的导出逻辑,或者将导出逻辑移到common模块
            // 当前临时实现:返回提示信息
            
            log.warn("异步导出功能待实现,请使用同步导出接口: /base/workflow/task/manage/exportTaskManage");
            saveErrorResult(taskId, "异步导出功能待完善,请使用同步导出接口");
            
        } catch (Exception e) {
            log.error("异步导出任务 {} 失败: {}", taskId, e.getMessage(), e);
            saveErrorResult(taskId, e.getMessage());
        }
    }

    /**
     * 更新导出进度
     */
    private void updateProgress(String taskId, int progress, String message) {
        Map<String, Object> progressInfo = new HashMap<>();
        progressInfo.put("progress", progress);
        progressInfo.put("message", message);
        progressInfo.put("timestamp", System.currentTimeMillis());
        
        redisTemplate.opsForHash().putAll("export:progress:" + taskId, progressInfo);
        redisTemplate.expire("export:progress:" + taskId, 30, TimeUnit.MINUTES);

        // 发布进度更新事件（可用于WebSocket推送）
        // messagingTemplate.convertAndSend("/topic/export/" + taskId, progressInfo);
    }

    /**
     * 保存导出结果
     */
    private void saveExportResult(String taskId, byte[] data) {
        // 保存文件数据
        redisTemplate.opsForValue().set("export:data:" + taskId, data, 30, TimeUnit.MINUTES);

        // 保存元信息
        Map<String, Object> metaInfo = new HashMap<>();
        metaInfo.put("status", "completed");
        metaInfo.put("fileSize", data.length);
        metaInfo.put("completedTime", System.currentTimeMillis());
        
        redisTemplate.opsForHash().putAll("export:meta:" + taskId, metaInfo);
        redisTemplate.expire("export:meta:" + taskId, 30, TimeUnit.MINUTES);
    }

    /**
     * 保存空结果
     */
    private void saveEmptyResult(String taskId) {
        Map<String, Object> metaInfo = new HashMap<>();
        metaInfo.put("status", "empty");
        metaInfo.put("message", "暂无数据可导出");
        metaInfo.put("completedTime", System.currentTimeMillis());
        
        redisTemplate.opsForHash().putAll("export:meta:" + taskId, metaInfo);
        redisTemplate.expire("export:meta:" + taskId, 30, TimeUnit.MINUTES);
    }

    /**
     * 保存错误信息
     */
    private void saveErrorResult(String taskId, String errorMessage) {
        Map<String, Object> metaInfo = new HashMap<>();
        metaInfo.put("status", "error");
        metaInfo.put("errorMessage", errorMessage);
        metaInfo.put("completedTime", System.currentTimeMillis());
        
        redisTemplate.opsForHash().putAll("export:meta:" + taskId, metaInfo);
        redisTemplate.expire("export:meta:" + taskId, 30, TimeUnit.MINUTES);
    }

    /**
     * 获取导出进度
     */
    public Map<String, Object> getExportProgress(String taskId) {
        Map<Object, Object> progress = redisTemplate.opsForHash().entries("export:progress:" + taskId);
        Map<Object, Object> meta = redisTemplate.opsForHash().entries("export:meta:" + taskId);

        Map<String, Object> result = new HashMap<>();
        progress.forEach((k, v) -> result.put(String.valueOf(k), v));
        meta.forEach((k, v) -> result.put(String.valueOf(k), v));
        return result;
    }

    /**
     * 获取导出结果
     */
    public byte[] getExportResult(String taskId) {
        return (byte[]) redisTemplate.opsForValue().get("export:data:" + taskId);
    }

    /**
     * 检查导出任务是否存在
     */
    public boolean isTaskExists(String taskId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("export:meta:" + taskId));
    }
}