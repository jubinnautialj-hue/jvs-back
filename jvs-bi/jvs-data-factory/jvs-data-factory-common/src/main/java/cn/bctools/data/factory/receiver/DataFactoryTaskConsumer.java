package cn.bctools.data.factory.receiver;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.config.AsyncTaskComponent;
import cn.bctools.data.factory.constant.RedisKey;
import cn.bctools.data.factory.dto.DataFactoryTaskMqDto;
import cn.bctools.data.factory.entity.JvsDataFactoryQueue;
import cn.bctools.data.factory.entity.enums.QueueTaskStatusEnum;
import cn.bctools.data.factory.enums.ImPushTypeEnums;
import cn.bctools.data.factory.im.MessageImPush;
import cn.bctools.data.factory.listener.task.FactoryTaskEvent;
import cn.bctools.data.factory.service.JvsDataFactoryQueueService;
import cn.bctools.data.factory.util.RabbitMqUtils;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author xiaohui
 */
@Slf4j
@Component
@Lazy(false)
public class DataFactoryTaskConsumer {
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    JvsDataFactoryQueueService jvsDataFactoryQueueService;
    @Autowired
    RedisUtils redisUtils;
    @Autowired
    AsyncTaskComponent asyncTaskComponent;
    @Autowired
    MessageImPush messageImPush;
    @Autowired
    ApplicationEventPublisher applicationEventPublisher;

    @SneakyThrows
    @RabbitListener(queues = RabbitMqUtils.QUEUE_DATA_FACTORY_TASK, concurrency = "2")
    public void receiveMessage(Message message, Channel channel) {
        //清空线程数据
        SystemThreadLocal.clear();
        FactoryTaskEvent factoryTaskEvent = JSONObject.parseObject(message.getBody(), FactoryTaskEvent.class);
        log.warn("-----接收到消息为:{}", factoryTaskEvent);
        //异步执行逻辑
        asyncTaskComponent.executeTask(factoryTaskEvent);
        //手动ack
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
    }

    /**
     * 消息发送
     *
     * @param dataFactoryTaskMqDto 消息体内容
     */
    @Transactional(rollbackFor = Exception.class)
    public void send(DataFactoryTaskMqDto dataFactoryTaskMqDto) {
        boolean tryLock = redisUtils.tryLock(RedisKey.getDataFactoryQueueLockKey(dataFactoryTaskMqDto.getDataFactoryId()), 1800);
        JvsDataFactoryQueue jvsDataFactoryQueue = new JvsDataFactoryQueue()
                .setDataFactoryId(dataFactoryTaskMqDto.getDataFactoryId())
                .setTaskStatus(QueueTaskStatusEnum.QUEUE)
                .setExecutionGraph(dataFactoryTaskMqDto.getExecutionGraph())
                .setBatchId(dataFactoryTaskMqDto.getBatchId())
                .setQueueTaskType(dataFactoryTaskMqDto.getQueueTaskType())
                .setTaskItselfId(dataFactoryTaskMqDto.getTaskItselfId())
                .setOperateMethod(dataFactoryTaskMqDto.getOperateMethod())
                .setPriority(dataFactoryTaskMqDto.getPriority())
                .setPrincipalName(dataFactoryTaskMqDto.getUserDto().getRealName());
        if (tryLock) {
            if (StrUtil.isBlank(TenantContextHolder.getTenantId())) {
                throw new BusinessException("租户id为空 无法创建数据");
            }
            //添加队列
            jvsDataFactoryQueueService.save(jvsDataFactoryQueue);
            dataFactoryTaskMqDto.setDataFactoryQueueId(jvsDataFactoryQueue.getId());
            try {
                applicationEventPublisher.publishEvent(JSONObject.parseObject(JSONObject.toJSONString(dataFactoryTaskMqDto), FactoryTaskEvent.class));
                //websocket 通知
                messageImPush.notify(ImPushTypeEnums.update, "", "");
            } catch (Exception exception) {
                log.info("mq入队列失败", exception);
                jvsDataFactoryQueue.setTaskStatus(QueueTaskStatusEnum.FAIL).setErrorMsg("mq入队列失败!");
            } finally {
                redisUtils.unLock(RedisKey.getDataFactoryQueueLockKey(dataFactoryTaskMqDto.getDataFactoryId()));
            }
        } else {
            //生成一个错误的日志
            jvsDataFactoryQueue.setTaskStatus(QueueTaskStatusEnum.FAIL)
                    .setErrorMsg("创建任务失败");
        }
        if (jvsDataFactoryQueue.getTaskStatus().equals(QueueTaskStatusEnum.FAIL)) {
            jvsDataFactoryQueueService.updateById(jvsDataFactoryQueue);
            throw new BusinessException("任务生成失败,请稍候再试");
        }
    }
}
