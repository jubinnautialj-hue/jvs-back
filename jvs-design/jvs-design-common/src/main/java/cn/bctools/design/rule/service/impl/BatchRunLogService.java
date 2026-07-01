package cn.bctools.design.rule.service.impl;

import cn.bctools.design.rule.entity.RunLogPo;
import cn.bctools.design.rule.service.RunLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 批量保存规则执行日志服务
 * 使用内存队列收集日志，定期批量写入数据库，减少数据库连接占用
 * 
 * @author assistant
 */
@Slf4j
@Service
public class BatchRunLogService {

    private final RunLogService runLogService;

    /**
     * 日志队列容量
     */
    @Value("${rule.log.queue.capacity:10000}")
    private int queueCapacity;

    /**
     * 批量保存大小
     */
    @Value("${rule.log.batch.size:100}")
    private int batchSize;

    /**
     * 是否启用批量保存
     */
    @Value("${rule.log.batch.enabled:true}")
    private boolean batchEnabled;

    /**
     * 日志队列
     */
    private BlockingQueue<RunLogPo> logQueue;

    /**
     * 服务运行标志
     */
    private volatile boolean running = true;

    public BatchRunLogService(RunLogService runLogService) {
        this.runLogService = runLogService;
    }

    @PostConstruct
    public void init() {
        if (!batchEnabled) {
            log.info("批量日志保存功能已禁用");
            return;
        }
        logQueue = new LinkedBlockingQueue<>(queueCapacity);
        log.info("批量日志保存服务初始化完成，队列容量: {}, 批量大小: {}", queueCapacity, batchSize);
    }

    /**
     * 添加日志到队列
     * 
     * @param runLogPo 日志对象
     */
    public void addLog(RunLogPo runLogPo) {
        if (!batchEnabled) {
            // 如果未启用批量保存，直接调用原方法
            runLogService.saveLog(runLogPo);
            return;
        }

        boolean offered = logQueue.offer(runLogPo);
        if (!offered) {
            log.warn("日志队列已满，当前大小: {}，直接保存日志", logQueue.size());
            // 队列满了，直接保存
            runLogService.saveLog(runLogPo);
        }
    }

    /**
     * 定时批量保存日志
     * 每5秒执行一次
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void scheduledBatchSave() {
        if (!batchEnabled || logQueue == null) {
            return;
        }
        batchSaveLogs();
    }

    /**
     * 批量保存日志
     */
    private void batchSaveLogs() {
        if (logQueue.isEmpty()) {
            return;
        }

        List<RunLogPo> batch = new ArrayList<>(batchSize);
        logQueue.drainTo(batch, batchSize);

        if (batch.isEmpty()) {
            return;
        }

        try {
            log.debug("开始批量保存日志，数量: {}", batch.size());
            long startTime = System.currentTimeMillis();
            
            // 批量保存
            runLogService.saveBatch(batch);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("批量保存日志成功，数量: {}, 耗时: {}ms, 剩余队列: {}", 
                batch.size(), duration, logQueue.size());
        } catch (Exception e) {
            log.error("批量保存日志失败，数量: {}", batch.size(), e);
            // 保存失败，尝试逐个保存
            for (RunLogPo logPo : batch) {
                try {
                    runLogService.saveLog(logPo);
                } catch (Exception ex) {
                    log.error("单条保存日志失败: {}", logPo.getId(), ex);
                }
            }
        }
    }

    /**
     * 应用关闭时保存剩余日志
     */
    @PreDestroy
    public void destroy() {
        if (!batchEnabled || logQueue == null) {
            return;
        }

        running = false;
        log.info("应用关闭中，开始保存剩余日志，当前队列大小: {}", logQueue.size());

        // 保存所有剩余日志
        while (!logQueue.isEmpty()) {
            batchSaveLogs();
        }

        log.info("剩余日志保存完成");
    }

    /**
     * 获取当前队列大小
     */
    public int getQueueSize() {
        return logQueue != null ? logQueue.size() : 0;
    }
}
