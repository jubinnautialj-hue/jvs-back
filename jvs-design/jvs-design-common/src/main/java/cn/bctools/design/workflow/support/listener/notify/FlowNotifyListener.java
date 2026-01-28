package cn.bctools.design.workflow.support.listener.notify;

import cn.bctools.design.notice.handler.DataNoticeHandler;
import cn.bctools.design.workflow.entity.FlowTask;
import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author zhuxiaokang
 * 流程相关通知
 */
@Slf4j
@Component
@AllArgsConstructor
public class FlowNotifyListener implements ApplicationRunner {

    private final DataNoticeHandler dataNoticeHandler;
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final BlockingQueue<FlowNotifyEvent> TASK_NOTIFY_QUEUE = new LinkedBlockingQueue<>();

    @Override
    public void run(ApplicationArguments args) {
        executorService.execute(this::consumerTaskQueue);
    }

    /**
     * 监听同步流程数据到业务事件
     *
     * @param event 事件对象
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotifyEvent(FlowNotifyEvent event) {
        // 加入队列
        TASK_NOTIFY_QUEUE.offer(event);

        if (log.isDebugEnabled()) {
            log.debug("监听事件[FlowNotifyEvent]: {}, 事件队列数量：{}", JSON.toJSONString(event), TASK_NOTIFY_QUEUE.size());
        }
    }

    /**
     * 消费队列
     */
    private void consumerTaskQueue() {
        while (true) {
            try {
                FlowNotifyEvent flowNotifyEvent = TASK_NOTIFY_QUEUE.take();
                FlowTask flowTask = flowNotifyEvent.getFlowTask();
                dataNoticeHandler.sendNotify(flowNotifyEvent.getTenantId(), flowTask.getJvsAppId(), flowNotifyEvent.getTriggerType(), flowTask.getDataModelId(), flowTask.getDataId(), flowTask, flowNotifyEvent.getNodeIds());
            } catch (Exception e) {
                log.error("发送流程通知异常：" + e);
            }
        }
    }
}
