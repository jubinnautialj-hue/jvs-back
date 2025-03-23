package cn.bctools.data.factory.listener.task;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.util.RabbitMqUtils;
import cn.bctools.message.push.api.InsideNotificationApi;
import cn.bctools.message.push.dto.messagepush.InsideNotificationDto;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Arrays;
import java.util.List;

/**
 * @author Administrator
 */
@Slf4j
@Component
@AllArgsConstructor
public class FactoryTaskListener {

    private final RabbitTemplate rabbitTemplate;
    private final InsideNotificationApi api;

    /**
     * 监听任务执行入队列
     *
     * @param event 事件对象
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskEndEvent(FactoryTaskEvent event) {
        rabbitTemplate.convertAndSend(RabbitMqUtils.QUEUE_DATA_FACTORY_TASK, event);
    }

    /**
     * 监听任务执行入队列
     *
     * @param event 事件对象
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void rollBackTask(FactoryTaskEvent event) {
        InsideNotificationDto interiorMessage = new InsideNotificationDto();
        Dict set = Dict.create().set("title", "任务进入队列").set("content", "任务事务回滚");
        interiorMessage.setContent(JSONObject.toJSONString(set));
        interiorMessage.setClientCode(SpringContextUtil.getApplicationContextName());
        interiorMessage.setTenantId("1");
        List<ReceiversDto> list = Arrays.asList(new ReceiversDto().setUserId("1").setUserName("管理员"));
        interiorMessage.setDefinedReceivers(list);
        api.send(interiorMessage);
    }

}
