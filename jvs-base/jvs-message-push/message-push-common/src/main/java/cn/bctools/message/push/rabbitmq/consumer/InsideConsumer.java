package cn.bctools.message.push.rabbitmq.consumer;

import cn.bctools.message.push.dto.enums.MessageTypeEnum;
import cn.bctools.message.push.dto.enums.PlatformEnum;
import cn.bctools.message.push.dto.messagepush.InsideNotificationDto;
import cn.bctools.message.push.handler.InsideNotificationHandler;
import cn.bctools.message.push.utils.MessagePushHisUtils;
import cn.bctools.message.push.utils.RabbitMqUtils;
import cn.bctools.rabbit.config.MyRabbitConfig;
import com.alibaba.fastjson2.JSON;
import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wl
 */
@Slf4j
@Component
@Lazy(false)
@AllArgsConstructor
public class InsideConsumer {
    private final MessagePushHisUtils messagePushHisUtils;
    private final InsideNotificationHandler insideNotificationHandler;

    @RabbitListener(queues = RabbitMqUtils.QUEUE_INFORM_INSIDE, containerFactory = MyRabbitConfig.BATCH_LISTENER_CONTAINER_FACTORY, concurrency = "1-5")
    public void receiveMessage(Channel channel, List<Message> messages) throws IOException {
        log.info("-----初始化发送站内信");
        try {
            List<InsideNotificationDto> logs = messages.stream().map(msg -> JSON.parseObject(msg.getBody(), InsideNotificationDto.class)).collect(Collectors.toList());
            //转换类
            logs.forEach(e -> {
                String batchNumber = messagePushHisUtils.saveHis(e, PlatformEnum.INSIDE_NOTIFICATION, MessageTypeEnum.INSIDE_NOTIFICATION);
                insideNotificationHandler.handle(batchNumber);
            });
            log.info("-----站内信发送完成");
        } catch (Exception e) {
            log.error(" send error", e);
        } finally {
            channel.basicAck(messages.get(messages.size() - 1).getMessageProperties().getDeliveryTag(), true);
        }
    }
}
