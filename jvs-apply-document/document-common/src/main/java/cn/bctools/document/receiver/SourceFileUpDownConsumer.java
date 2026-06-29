package cn.bctools.document.receiver;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.document.config.CommonConfig;
import cn.bctools.document.entity.SourceFileUdLog;
import cn.bctools.document.entity.enums.SourceFileUdOperateStatusEnums;
import cn.bctools.document.receiver.dto.DownFileTaskDto;
import cn.bctools.document.receiver.dto.UpFileTaskDto;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.document.service.SourceFileUdLogService;
import cn.bctools.document.util.RabbitMqUtils;
import cn.bctools.oss.dto.BaseFile;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 源文件队列
 *
 * @author Administrator
 */
@Slf4j
@Component
@AllArgsConstructor
public class SourceFileUpDownConsumer {
    private final RabbitTemplate rabbitTemplate;
    private final DcLibraryService dcLibraryService;
    private final CommonConfig commonConfig;
    private final SourceFileUdLogService sourceFileUdLogService;


    @RabbitListener(queues = RabbitMqUtils.QUEUE_SOURCE_FILE_DOWN_TASK, concurrency = "1")
    public void downFile(Message message, Channel channel) throws IOException {
        String errorMsg = null;
        String errorLog = null;
        BaseFile baseFile = null;
        DownFileTaskDto downFileTaskDto = JSONObject.parseObject(message.getBody(), DownFileTaskDto.class);
        try {
            baseFile = dcLibraryService.downSourceFile(downFileTaskDto.getId());
        } catch (Exception exception) {
            log.error("下载文件其他错误", exception);
            if (exception.getCause() instanceof BusinessException) {
                errorMsg = exception.getCause().getMessage();
            } else {
                errorMsg = "未知错误";
            }
            errorLog = exception.getMessage();
        } finally {
            //删除整个目录中的所有文件
            FileUtil.del(commonConfig.getDownPath());
            //修改日志
            boolean notBlank = StrUtil.isNotBlank(errorMsg);
            sourceFileUdLogService.update(new UpdateWrapper<SourceFileUdLog>().lambda()
                    .set(SourceFileUdLog::getOperateStatus, notBlank ? SourceFileUdOperateStatusEnums.fail : SourceFileUdOperateStatusEnums.success)
                    .set(!notBlank, SourceFileUdLog::getFileName, baseFile.getFileName())
                    .set(!notBlank, SourceFileUdLog::getBucketName, baseFile.getBucketName())
                    .set(notBlank, SourceFileUdLog::getErrorLog, errorLog)
                    .set(notBlank, SourceFileUdLog::getErrorMessage, errorMsg)
                    .eq(SourceFileUdLog::getId, downFileTaskDto.getLogId()));
            //手动ack
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        }

    }

    @RabbitListener(queues = RabbitMqUtils.QUEUE_SOURCE_FILE_UP_TASK, concurrency = "1")
    public void upFile(Message message, Channel channel) throws IOException {
        //清空线程数据
        SystemThreadLocal.clear();
        UpFileTaskDto upFileTaskDto = JSONObject.parseObject(message.getBody(), UpFileTaskDto.class);
        TenantContextHolder.setTenantId(upFileTaskDto.getTenantId());
        BaseFile baseFile = new BaseFile().setFileName(upFileTaskDto.getFileName())
                .setBucketName(upFileTaskDto.getBucketName());
        dcLibraryService.upSourceFile(baseFile, upFileTaskDto.getParentId(),upFileTaskDto.getUserDto(),upFileTaskDto.getLogId());
        //手动ack
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
    }


    /**
     * 下载数据消息发送
     *
     * @param downFileTaskDto 消息对象
     */
    public void sendDownTask(DownFileTaskDto downFileTaskDto) {
        rabbitTemplate.convertAndSend(RabbitMqUtils.QUEUE_SOURCE_FILE_DOWN_TASK, downFileTaskDto);
    }

    /**
     * 下载数据消息发送
     *
     * @param upFileTaskDto 消息对象
     */
    public void sendUpTask(UpFileTaskDto upFileTaskDto) {
        rabbitTemplate.convertAndSend(RabbitMqUtils.QUEUE_SOURCE_FILE_UP_TASK, upFileTaskDto);
    }
}
