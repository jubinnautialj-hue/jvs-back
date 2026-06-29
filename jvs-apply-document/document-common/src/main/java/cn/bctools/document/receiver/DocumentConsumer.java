package cn.bctools.document.receiver;

import cn.bctools.common.utils.IdGenerator;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.document.component.AsyncComponent;
import cn.bctools.document.constant.Constant;
import cn.bctools.document.listener.es.DocumentMqEvent;
import cn.bctools.document.receiver.dto.DocumentMqDto;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.document.util.RabbitMqUtils;
import cn.bctools.document.util.RedisLockUtil;
import cn.bctools.message.push.api.InsideNotificationApi;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.redis.utils.RedisUtils;
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
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Locale;

/**
 * @author xiaohui
 */
@Slf4j
@Component
@Lazy(false)
public class DocumentConsumer {
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    RedisUtils redisUtils;
    @Autowired
    RedisLockUtil redisLockUtil;
    @Autowired
    InsideNotificationApi insideNotificationApi;
    @Autowired
    AsyncComponent asyncComponent;
    @Autowired
    ApplicationEventPublisher applicationEventPublisher;


    @SneakyThrows
    @RabbitListener(queues = RabbitMqUtils.QUEUE_DOCUMENT_FILE_TO_ES_TASK, concurrency = "5")
    public void fileToEsReceiveMessage(Message message, Channel channel) {
        DocumentMqEvent documentMqEvent = JSONObject.parseObject(message.getBody(), DocumentMqEvent.class);
        //设置租户id
        TenantContextHolder.setTenantId(documentMqEvent.getTenantId());
        log.info("-----接收到消息开始:{}", documentMqEvent);
        //防止读取时间超过mq的执行时间每次执行都需要把当前文档id放入redis中防止重新识别
        if (redisLockUtil.GetQueueFileToEsTaskLock(documentMqEvent.getDocumentId(), documentMqEvent.getTenantId())) {
            DcLibraryService dcLibraryService = SpringContextUtil.getBean(DcLibraryService.class);
            log.info("线程名称:{},获取对象{}", Thread.currentThread().getName(), dcLibraryService.toString());
            //判断是否需要转换文件  目前支持  doc->docx  xls->xlsx  系统自动转换
            dcLibraryService.fileTransition(documentMqEvent.getDocumentId());
            //异步执行逻辑
            asyncComponent.asyncDcLibraryFileToSave(documentMqEvent);
            log.info("-----接收到消息结束:{}", documentMqEvent);
            redisLockUtil.UnLockQueueFileToEsTaskLock(documentMqEvent.getDocumentId(), documentMqEvent.getTenantId());
            //
            //手动ack
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


    @SneakyThrows
    @RabbitListener(queues = RabbitMqUtils.QUEUE_DOCUMENT_FILE_DECOMPRESSION_TASK, concurrency = "1")
    public void fileDecompressionReceiveMessage(Message message, Channel channel) {
        DocumentMqDto documentMqDto = JSONObject.parseObject(message.getBody(), DocumentMqDto.class);
        String format = String.format(Constant.LOCK_MQ_DECOMPRESSION_KEY, documentMqDto.getUuid());
        if (!redisUtils.exists(format)) {
            redisUtils.set(format, documentMqDto.getDocumentId(), 4 * 60 * 60L);
            asyncComponent.asyncUnzip(documentMqDto, documentMqDto.getUserDto());
        }
        log.info("-----接收到消息为:{}", documentMqDto);
        //手动ack
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    /**
     * 消息发送 同步es
     *
     * @param documentMqEvent 消息体
     */
    public void sendFileToEs(DocumentMqEvent documentMqEvent) {
        try {
            boolean isInTransaction = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("当前是否存在事务{}", isInTransaction);
            Locale locale = LocaleContextHolder.getLocale();
            documentMqEvent.setLocale(locale);
            //统一设置一个唯一uuid
            documentMqEvent.setUuid(IdGenerator.getIdStr());
            if (isInTransaction) {
                //等待事务提交后执行
                applicationEventPublisher.publishEvent(documentMqEvent);
            } else {
                rabbitTemplate.convertAndSend(RabbitMqUtils.QUEUE_DOCUMENT_FILE_TO_ES_TASK, documentMqEvent);
            }
        } catch (Exception exception) {
            log.error("同步es消息发送失败:", exception);
        }
    }

    /**
     * 消息发送 解压
     *
     * @param id 需要解压的文档id
     */
    public void sendFileDecompression(String id) {
        try {
            DocumentMqDto documentMqDto = new DocumentMqDto().setLocale(LocaleContextHolder.getLocale())
                    .setDocumentId(id)
                    .setUserDto(UserCurrentUtils.getCurrentUser())
                    .setTenantId(TenantContextHolder.getTenantId())
                    .setUuid(IdGenerator.getIdStr());
            rabbitTemplate.convertAndSend(RabbitMqUtils.QUEUE_DOCUMENT_FILE_DECOMPRESSION_TASK, documentMqDto);
        } catch (Exception exception) {
            log.error("解压消息发送失败:", exception);
        }
    }
}
