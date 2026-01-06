package cn.bctools.design.workflow.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步导出配置类
 * 配置导出任务专用的线程池
 *
 * @author BCTools
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncExportConfig {

    /**
     * 导出任务执行器
     * 用于处理大数据量的异步导出任务
     */
    @Bean("exportExecutor")
    public Executor exportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数：2个
        executor.setCorePoolSize(2);

        // 最大线程数：5个
        executor.setMaxPoolSize(5);

        // 队列容量：100个
        executor.setQueueCapacity(100);

        // 线程名称前缀
        executor.setThreadNamePrefix("export-thread-");

        // 拒绝策略：由调用线程处理（保证不丢失任务）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 设置线程池关闭时的等待时间
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        // 初始化
        executor.initialize();

        log.info("异步导出线程池初始化完成 - 核心线程数: {}, 最大线程数: {}, 队列容量: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }

    /**
     * 轻量级异步执行器
     * 用于处理快速的小任务
     */
    @Bean("lightExecutor")
    public Executor lightExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数：1个
        executor.setCorePoolSize(1);

        // 最大线程数：3个
        executor.setMaxPoolSize(3);

        // 队列容量：50个
        executor.setQueueCapacity(50);

        // 线程名称前缀
        executor.setThreadNamePrefix("light-thread-");

        // 拒绝策略：丢弃最旧的任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());

        // 初始化
        executor.initialize();

        log.info("轻量级异步执行器初始化完成 - 核心线程数: {}, 最大线程数: {}, 队列容量: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }
}