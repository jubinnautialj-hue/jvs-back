package cn.bctools.document.listener.es;

import cn.bctools.document.util.RabbitMqUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@AllArgsConstructor
public class FactoryTaskListener {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 文件同步到es 需要等待事务提交后执行
     *
     * @param event 事件对象
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskEndEvent(DocumentMqEvent event) {
        rabbitTemplate.convertAndSend(RabbitMqUtils.QUEUE_DOCUMENT_FILE_TO_ES_TASK, event);
    }

}
