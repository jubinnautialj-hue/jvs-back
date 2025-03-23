package cn.bctools.data.factory.receiver;

import cn.bctools.data.factory.consanguinity.view.entity.ConsanguinityViewEntity;
import cn.bctools.data.factory.consanguinity.view.service.ConsanguinityViewService;
import cn.bctools.data.factory.entity.ConsanguinityAnalyse;
import cn.bctools.data.factory.util.RabbitMqUtils;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

/**
 * @author Administrator
 */
@Slf4j
@Component
@AllArgsConstructor
public class ConsanguinityAnalyseConsumer {

    private final ConsanguinityViewService consanguinityViewService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMqUtils.CONSANGUINITY_ANALYSE_TASK, concurrency = "5")
    public void receiveMessage(Message message, Channel channel) throws IOException {
        log.info("----血缘视图记录");
        try {
            //转换类
            ConsanguinityAnalyse consanguinityAnalyse = JSONObject.parseObject(message.getBody(), ConsanguinityAnalyse.class);
            String id=consanguinityAnalyse.getDataFactoryId();
            if (consanguinityAnalyse.getType()!=2){
                id = consanguinityAnalyse.getDesignId() + consanguinityAnalyse.getDataFactoryId();
                if (StrUtil.isNotEmpty(consanguinityAnalyse.getDesignDetailId())) {
                    id += consanguinityAnalyse.getDesignDetailId();
                }
                id = SecureUtil.md5(id);
            }
            //组装数据
            ConsanguinityViewEntity consanguinityViewEntity = new ConsanguinityViewEntity()
                    .setDesignId(consanguinityAnalyse.getDesignId())
                    .setId(id)
                    .setTitle(consanguinityAnalyse.getDesignName())
                    .setSubordinateTile(consanguinityAnalyse.getDesignDetailName())
                    .setSubordinateId(consanguinityAnalyse.getDesignDetailId())
                    .setType(consanguinityAnalyse.getType())
                    .setGroupId(consanguinityAnalyse.getDataFactoryId())
                    .setTenantId(consanguinityAnalyse.getTenantId())
                    .setViewType(consanguinityAnalyse.getViewType());
            consanguinityViewService.save(Collections.singletonList(consanguinityViewEntity));
        } catch (Exception e) {
            log.error(" 血缘视图队列消费错误", e);
        }
        //手动ack
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        log.info("-----血缘视图队列消费成功--完成");
    }

    /**
     * 消息发送
     *
     * @param consanguinityAnalyse 消息体内容
     */
    public void send(ConsanguinityAnalyse consanguinityAnalyse) {
        rabbitTemplate.convertAndSend(RabbitMqUtils.CONSANGUINITY_ANALYSE_TASK, consanguinityAnalyse);
    }
}
