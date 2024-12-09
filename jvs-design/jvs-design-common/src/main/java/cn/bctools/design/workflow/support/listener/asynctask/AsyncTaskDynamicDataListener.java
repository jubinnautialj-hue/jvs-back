package cn.bctools.design.workflow.support.listener.asynctask;

import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.enums.FlowTaskEventTypeEnum;
import cn.bctools.design.workflow.support.listener.AbstractCompensateEvent;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.*;

/**
 * @author zhuxiaokang
 * 同步流程数据到业务
 */
@Slf4j
@Component
@AllArgsConstructor
public class AsyncTaskDynamicDataListener extends AbstractCompensateEvent implements ApplicationRunner {

    private final AsyncTaskDynamicDataMqHandler handler;
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final BlockingQueue<FlowTask> TASK_QUEUE = new LinkedBlockingQueue<>();

    @Override
    public void run(ApplicationArguments args) {
        executorService.execute(this::consumerTaskQueue);
        timingCompensate(FlowTaskEventTypeEnum.ASYNC_TASK_TO_DATA);
    }

    /**
     * 监听同步流程数据到业务事件
     *
     * @param event 事件对象
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAsyncTaskEvent(AsyncTaskDynamicDataEvent event) {
        addQueue(event.getFlowTask());

        if (log.isDebugEnabled()) {
            log.debug("监听事件[AsyncTaskDynamicDataEvent]：{}, 待消费事件数量：{}", JSON.toJSONString(event), TASK_QUEUE.size());
        }
    }

    private void addQueue(FlowTask flowTask) {
        FlowTask task = new FlowTask()
                .setId(flowTask.getId())
                .setTaskStatus(flowTask.getTaskStatus())
                .setDataModelId(flowTask.getDataModelId())
                .setDataId(flowTask.getDataId());
        TASK_QUEUE.offer(task);
    }


    /**
     * 消费队列
     */
    private void consumerTaskQueue() {
        while (true) {
            try {
                // 这个方法会阻塞，直到有任务可用
                handler.send(TASK_QUEUE.take());
            } catch (Exception e) {
                log.error("同步流程数据到业务异常：" + e);
            }
        }
    }


    @Override
    public void compensateEvent(String eventBody) {
        FlowTask flowTask = JSONObject.parseObject(eventBody, FlowTask.class);
        addQueue(flowTask);
    }
}
