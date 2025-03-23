package cn.bctools.im.message;

import cn.bctools.im.message.constant.RabbitMqConstant;
import cn.bctools.im.message.dto.BaseSyncMessageDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author ZhuXiaoKang
 * @Description
 */

@Component
public class RabbitMqTemplate {

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     *
     * @param <T> <T extends MessageDto>
     */
    public <T extends BaseSyncMessageDto> void send(T t) {
        rabbitTemplate.convertAndSend(RabbitMqConstant.EXCHANGE_SYNC_MESSAGE, t.getTopic(), t);
    }
}
