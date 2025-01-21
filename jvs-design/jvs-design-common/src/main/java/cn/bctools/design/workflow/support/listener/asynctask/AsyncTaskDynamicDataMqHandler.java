package cn.bctools.design.workflow.support.listener.asynctask;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.enums.FlowTaskEventTypeEnum;
import cn.bctools.design.workflow.service.FlowDynamicDataService;
import cn.bctools.design.workflow.service.FlowTaskEventCompensateService;
import cn.bctools.rabbit.config.MyRabbitConfig;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 同步流程数据到业务MQ
 */
@Slf4j
@Component
@AllArgsConstructor
public class AsyncTaskDynamicDataMqHandler {

    private final FlowDynamicDataService flowDynamicDataService;
    private final FlowTaskEventCompensateService flowTaskEventCompensateService;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 发布同步流程数据到业务消息
     *
     * @param flowTask 流程任务
     */
    public void send(FlowTask flowTask) {
        try {
            rabbitTemplate.convertAndSend(AsyncTaskDynamicDataMqConfig.ASYNC_TASK_DATA_EXCHANGE, AsyncTaskDynamicDataMqConfig.ASYNC_TASK_DATA_ROUTING, flowTask);
        } catch (Exception e) {
            log.error("发布同步流程数据到业务消息到MQ异常：", e);
            log.info("持久化消息到数据库");
            flowTaskEventCompensateService.save(FlowTaskEventTypeEnum.ASYNC_TASK_TO_DATA, JSONObject.toJSONString(flowTask));
        }

    }


    @SneakyThrows
    @RabbitListener(queues = AsyncTaskDynamicDataMqConfig.ASYNC_TASK_DATA_QUEUE, containerFactory = MyRabbitConfig.BATCH_LISTENER_CONTAINER_FACTORY, concurrency = "1-10")
    public void asyncTaskToDynamicData(Channel channel, List<Message> messages) {
        try {
            //不在线直接返回
            if (!SpringContextUtil.thisServerStats()) {
                channel.basicNack(messages.get(messages.size() - 1).getMessageProperties().getDeliveryTag(), true, true);
                return;
            }
            List<FlowTask> flowTasks = messages.stream().map(msg -> JSON.parseObject(msg.getBody(), FlowTask.class)).collect(Collectors.toList());
            flowDynamicDataService.saveTaskToModel(flowTasks);
            channel.basicAck(messages.get(messages.size() - 1).getMessageProperties().getDeliveryTag(), true);
        } catch (Exception e) {
            log.error("同步流程数据到业务，MQ消费异常：", e);
            channel.basicNack(messages.get(messages.size() - 1).getMessageProperties().getDeliveryTag(), true, true);
        }
    }
}
