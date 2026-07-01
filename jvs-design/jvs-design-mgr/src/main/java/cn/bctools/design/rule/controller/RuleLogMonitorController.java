package cn.bctools.design.rule.controller;

import cn.bctools.common.utils.R;
import cn.bctools.design.rule.config.RuleLogBatchConfig;
import cn.bctools.design.rule.service.impl.BatchRunLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 规则日志监控接口
 * 
 * @author assistant
 */
@Api(tags = "[rule]规则日志监控")
@RestController
@RequestMapping("/rule/log/monitor")
@AllArgsConstructor
public class RuleLogMonitorController {

    private final BatchRunLogService batchRunLogService;
    private final RuleLogBatchConfig ruleLogBatchConfig;

    /**
     * 获取批量日志队列状态
     */
    @ApiOperation("获取批量日志队列状态")
    @GetMapping("/queue/status")
    public R<QueueStatus> getQueueStatus() {
        QueueStatus status = new QueueStatus();
        status.setQueueSize(batchRunLogService.getQueueSize());
        status.setQueueCapacity(ruleLogBatchConfig.getQueueCapacity());
        status.setBatchSize(ruleLogBatchConfig.getBatchSize());
        status.setBatchEnabled(ruleLogBatchConfig.getBatchEnabled());
        status.setBatchInterval(ruleLogBatchConfig.getBatchInterval());
        
        // 计算队列使用率
        if (ruleLogBatchConfig.getQueueCapacity() > 0) {
            double usageRate = (double) status.getQueueSize() / ruleLogBatchConfig.getQueueCapacity() * 100;
            status.setUsageRate(String.format("%.2f%%", usageRate));
        }
        
        return R.ok(status);
    }

    /**
     * 队列状态DTO
     */
    @Data
    public static class QueueStatus {
        /**
         * 当前队列大小
         */
        private Integer queueSize;
        
        /**
         * 队列容量
         */
        private Integer queueCapacity;
        
        /**
         * 批量大小
         */
        private Integer batchSize;
        
        /**
         * 是否启用批量
         */
        private Boolean batchEnabled;
        
        /**
         * 批量间隔（毫秒）
         */
        private Long batchInterval;
        
        /**
         * 队列使用率
         */
        private String usageRate;
    }
}
