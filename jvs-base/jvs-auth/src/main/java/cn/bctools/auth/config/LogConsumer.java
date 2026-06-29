package cn.bctools.auth.config;

import cn.bctools.auth.service.SysLogService;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.dingding.DingDingConfig;
import cn.bctools.dingding.DingSendUtils;
import cn.bctools.log.config.LogMqConfig;
import cn.bctools.log.po.LogPo;
import cn.bctools.rabbit.config.MyRabbitConfig;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSON;
import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 日志MQ消费
 */
@Slf4j
@Component
@AllArgsConstructor
public class LogConsumer {

    private final DingDingConfig dingDingConfig;
    private final DingSendUtils sendUtils;
    private final SysLogService sysLogService;

    /**
     * 保存日志消费
     *
     * @return
     */
    @SneakyThrows
    @RabbitListener(queues = LogMqConfig.SYS_LOG_SAVE_QUEUE, containerFactory = MyRabbitConfig.BATCH_LISTENER_CONTAINER_FACTORY, concurrency = "1-20")
    public void saveLog(Channel channel, List<Message> messages) {
        try {
            //不在线直接返回
            if (!SpringContextUtil.thisServerStats()) {
                return;
            }
            List<LogPo> logs = messages.stream().map(msg -> JSON.parseObject(msg.getBody(), LogPo.class)).collect(Collectors.toList());
            sysLogService.saveBatch(logs);
            sendDing(logs);
        } catch (Exception e) {
            log.error("保存日志失败：{}", e.getMessage());
        } finally {
            channel.basicAck(messages.get(messages.size()-1).getMessageProperties().getDeliveryTag(), true);
        }
    }

    /**
     * 向钉钉发消息
     *
     * @param logs
     */
    private void sendDing(List<LogPo> logs) {
        if (dingDingConfig.getLog()) {
            logs.forEach(logPo -> {
                String str = null;
                try {
                    if (ObjectNull.isNotNull(logPo.getExceptionMessage())) {
                        str = "[tid] :" + logPo.getTid() +
                                "\n env : " + logPo.getEnv() +
                                "\n requestIP : " + logPo.getIp() +
                                "\n time : " + DateUtil.now() +
                                "\n" + logPo.getExceptionMessage();
                    }
                    if (ObjectNull.isNotNull(str) && str.length() > 10) {
                        //发送通知
                        sendUtils.sendMessage(str);
                    }
                } catch (Exception e) {
                    log.error("发送钉钉通知失败：{}, 通知内容：{}", e.getMessage(), str);
                }
            });
        }
    }
}
