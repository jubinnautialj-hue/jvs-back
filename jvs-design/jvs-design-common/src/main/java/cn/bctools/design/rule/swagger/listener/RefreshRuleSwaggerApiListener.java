package cn.bctools.design.rule.swagger.listener;

import cn.bctools.common.utils.BeanCopyUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * @author jvs
 * 刷新Swagger中的逻辑API事件通知监听
 */
@Slf4j
@Component
@AllArgsConstructor
public class RefreshRuleSwaggerApiListener {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 刷新Swagger中的逻辑API事件通知监听，广播消息
     *
     * @param event 事件对象
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAsyncTaskEvent(RefreshRuleSwaggerApiEvent event) {
        try {
            RefreshSwaggerApiMessage message = BeanCopyUtil.copy(event, RefreshSwaggerApiMessage.class);
            rabbitTemplate.convertAndSend(RefreshSwaggerApiMqConfig.EXCHANGE, "", message);
        } catch (Exception e) {
            log.error("广播刷新逻辑API到Swagger消息失败：", e);
        }
    }
}
