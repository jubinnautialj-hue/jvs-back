package cn.bctools.data.factory.receiver;

import cn.bctools.data.factory.notice.dto.NoticeCacheDto;
import cn.bctools.data.factory.notice.service.DataNoticeService;
import cn.bctools.data.factory.util.RabbitMqUtils;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@AllArgsConstructor
public class NoticeConsumer {

    private final DataNoticeService dataNoticeService;

    @RabbitListener(queues = RabbitMqUtils.QUEUE_DATA_FACTORY_TASK_NOTICE, concurrency = "5")
    public void receiveMessage(Message message, Channel channel) throws IOException {
        log.info("----发送信息");
        try {
            //转换类
            NoticeCacheDto noticeCacheDto = JSONObject.parseObject(message.getBody(), NoticeCacheDto.class);
            dataNoticeService.sendNotice(noticeCacheDto);
        } catch (Exception e) {
            log.error(" send error", e);
        }
        //手动ack
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        log.info("-----发送消息--完成");
    }
}
