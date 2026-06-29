package cn.bctools.message.push.handler.dingtalk.robot;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.dingding.DingDingConfig;
import cn.bctools.message.push.dto.config.DingTalkRobotConfig;
import cn.bctools.message.push.dto.enums.MessagePushStatusEnum;
import cn.bctools.message.push.dto.enums.MessageTypeEnum;
import cn.bctools.message.push.dto.enums.PlatformEnum;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import cn.bctools.message.push.dto.messagepush.dingtalk.robot.TextMessageDTO;
import cn.bctools.message.push.entity.MessagePushHis;
import cn.bctools.message.push.handler.BaseMessageHandler;
import cn.bctools.message.push.service.MessagePushHisService;
import cn.bctools.message.push.utils.MessagePushUtils;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson2.JSON;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.taobao.api.ApiException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 钉钉群机器人文本类型消息处理器
 *
 * @author wl*/
@Component
@Slf4j
@AllArgsConstructor
public class RobotTextMessageHandler extends BaseMessageHandler<TextMessageDTO> {

    private final MessagePushHisService messagePushHisService;
    private final DingDingConfig dingDingConfig;

    @Override
    public void handle(TextMessageDTO param) {
        if (!param.hasReceiver()) {
            throw new BusinessException("没有检测到接收人配置");
        }
        //获取配置详情
        DingTalkRobotConfig config = new DingTalkRobotConfig().setWebhook(dingDingConfig.getUrl() + dingDingConfig.getSecret());

        //获取接收人
        List<String> receiverUsers = param.getDefinedReceivers().stream().map(ReceiversDto::getReceiverConfig).collect(Collectors.toList());

        //生成批次号
        String batchNumber = UUID.randomUUID().toString().replaceAll("-", "");

        MessagePushHis messagePushHis = new MessagePushHis()
                .setBatchNumber(batchNumber)
                .setMessageContent(JSON.toJSONString(param))
                .setPlatform(PlatformEnum.DING_TALK_ROBOT)
                .setMessageType(MessageTypeEnum.DING_TALK_ROBOT_TEXT);

        try {
            DingTalkClient dingTalkClient = new DefaultDingTalkClient(config.getWebhook());
            OapiRobotSendRequest request = new OapiRobotSendRequest();
            request.setMsgtype("text");
            OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
            text.setContent(param.getContent());
            request.setText(text);
            OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
            at.setIsAtAll(param.getAll());
            at.setAtMobiles(MessagePushUtils.getMobile(receiverUsers));
            at.setAtUserIds(MessagePushUtils.getNotMobile(receiverUsers));
            request.setAt(at);
            OapiRobotSendResponse rsp = dingTalkClient.execute(request);
            if (!rsp.isSuccess()) {
                throw new IllegalStateException(rsp.getBody());
            }
            messagePushHis.setPushStatus(MessagePushStatusEnum.SUCCESS);
            messagePushHis.setErrorMsg(rsp.getBody());
        } catch (ApiException e) {
            log.error("api error",e);
            messagePushHis.setPushStatus(MessagePushStatusEnum.FAILED);
            String eMessage = ExceptionUtil.getMessage(e);
            eMessage = StringUtils.isBlank(eMessage) ? "未知错误" : eMessage;
            messagePushHis.setErrorMsg(eMessage);
        }
        messagePushHisService.save(messagePushHis);
    }

    @Override
    public void resend(String pushHisId) throws Exception {
        MessagePushHis his = messagePushHisService.getById(pushHisId);
        TextMessageDTO dto = JSON.parseObject(his.getMessageContent(), TextMessageDTO.class);
        handle(dto);
    }
}
