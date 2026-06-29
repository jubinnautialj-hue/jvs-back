package cn.bctools.message.push.handler;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.message.push.dto.enums.MessagePushStatusEnum;
import cn.bctools.message.push.dto.enums.MessageTypeEnum;
import cn.bctools.message.push.dto.enums.PlatformEnum;
import cn.bctools.message.push.dto.messagepush.InsideNotificationDto;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import cn.bctools.message.push.entity.InsideNotice;
import cn.bctools.message.push.entity.MessagePushHis;
import cn.bctools.message.push.service.InsideNoticeService;
import cn.bctools.message.push.service.MessagePushHisService;
import cn.bctools.message.push.websocket.InsideNoticeEndPoint;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 将站内消息推送到rabbitmq上 queue的名字为终端唯一标识符 交换机的名字为inside_notification 路由键前缀为inside_+终端唯一标识符
 *
 * @author czy
 */
@Slf4j
@Component
@AllArgsConstructor
public class InsideNotificationHandler extends BaseMessageHandler<InsideNotificationDto> {

    private final InsideNoticeEndPoint insideNoticeEndPoint;
    private final MessagePushHisService messagePushHisService;
    private final InsideNoticeService insideNoticeService;
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void handle(InsideNotificationDto param) {
        List<ReceiversDto> definedReceivers = param.getDefinedReceivers();
        if (definedReceivers.isEmpty()) {
            throw new BusinessException("没有检测到接收人配置");
        }
        //生成批次号
        String batchNumber = IdWorker.get32UUID();

        param.setRequestNo(batchNumber);
        List<MessagePushHis> messagePushHisList = new ArrayList<>();
        List<InsideNotice> noticeList = new ArrayList<>();

        definedReceivers.forEach(receiversDto -> {
            InsideNotice insideNotice = new InsideNotice()
                    .setReadIs(Boolean.FALSE)
                    .setUserId(receiversDto.getUserId())
                    .setUserName(receiversDto.getUserName())
                    .setMsgContent(param.getContent())
                    .setBatchNumber(batchNumber)
                    .setClientCode(param.getClientCode());
            noticeList.add(insideNotice);
        });
        insideNoticeService.saveBatch(noticeList);

        noticeList.forEach(notice -> {
            MessagePushHis messagePushHis = new MessagePushHis()
                    .setBatchNumber(batchNumber)
                    .setUserName(notice.getUserName())
                    .setUserId(notice.getUserId())
                    .setPlatform(PlatformEnum.INSIDE_NOTIFICATION)
                    .setMessageType(MessageTypeEnum.INSIDE_NOTIFICATION)
                    .setClientCode(param.getClientCode()).setMessageContent(JSON.toJSONString(param));
            try {
                this.sendMessageToRedis(JSONObject.toJSONString(notice));
                messagePushHis.setPushStatus(MessagePushStatusEnum.SUCCESS);

            } catch (Exception e) {
                log.error("消息发送异常",e);
                messagePushHis.setPushStatus(MessagePushStatusEnum.FAILED);
                String eMessage = ExceptionUtil.getMessage(e);
                eMessage = StringUtils.isBlank(eMessage) ? "未知错误" : eMessage;
                messagePushHis.setErrorMsg(eMessage);
            }
            messagePushHisList.add(messagePushHis);
        });

        messagePushHisService.saveBatch(messagePushHisList);
    }

    public void handle(String batchNumber) {
        List<InsideNotice> insideNoticeList = insideNoticeService.list(new LambdaQueryWrapper<InsideNotice>().eq(InsideNotice::getBatchNumber, batchNumber));
        if (insideNoticeList.isEmpty()) {
            log.info("当前批次号{}未找到消息记录", batchNumber);
            return;
        }
        insideNoticeList.forEach(notice -> {
            try {
                this.sendMessageToRedis(JSONObject.toJSONString(notice));
                log.info("批次号为{}接收人为{}的站内信发送成功", batchNumber, notice.getUserName());
            } catch (Exception e) {
                log.error("消息发送异常",e);
            }
        });
    }

    @Override
    public void resend(String pushHisId) {
        MessagePushHis his = messagePushHisService.getById(pushHisId);
        InsideNotificationDto dto = JSON.parseObject(his.getMessageContent(), InsideNotificationDto.class);
        dto.getDefinedReceivers().removeIf(e -> e.getUserId() == null || !e.getUserId().equals(his.getUserId()));
        handle(dto);
    }

    /**
     * 推送消息
     *
     * @param message
     */
    private void sendMessageToRedis(String message) {
        String channel = new ChannelTopic("messageCenter").getTopic();
        redisTemplate.convertAndSend(channel, message);
    }
}
