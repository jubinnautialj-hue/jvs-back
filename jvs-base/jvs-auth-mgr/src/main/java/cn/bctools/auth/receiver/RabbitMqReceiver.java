package cn.bctools.auth.receiver;

import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.auth.entity.Message;
import cn.bctools.auth.entity.UserExtension;
import cn.bctools.auth.login.enums.LoginTypeEnum;
import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.auth.service.UserExtensionService;
import cn.bctools.auth.service.UserService;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigSms;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.message.push.api.*;
import cn.bctools.message.push.dto.messagepush.AliSmsDto;
import cn.bctools.message.push.dto.messagepush.EmailMessageDto;
import cn.bctools.message.push.dto.messagepush.InsideNotificationDto;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import cn.bctools.message.push.dto.messagepush.dingtalk.corp.TextMessageDTO;
import cn.bctools.message.push.dto.messagepush.wechatofficialaccount.TemplateMessageDTO;
import cn.bctools.message.push.dto.messagepush.wechatofficialaccount.WechatTemplateData;
import cn.bctools.message.push.dto.messagepush.wechatwork.agent.WeTextMessageDTO;
import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Slf4j
@Component
@AllArgsConstructor
public class RabbitMqReceiver {

    AliSmsApi aliSmsApi;
    MessagePushHisApi messagePushHisApi;
    DingTalkCorpApi dingTalkCorpApi;
    EmailMessagePushApi emailMessagePushApi;
    WechatOfficialAccountApi wechatOfficialAccountApi;
    WechatWorkAgentApi wechatWorkAgentApi;
    UserExtensionService userExtensionService;
    UserService userService;
    SysConfigsService sysConfigService;
    InsideNotificationApi insideNotificationApi;
    public static final String KEY_MSG_SEND_INTERIOR = "msg-send-interior";
    public static final String QUEUE_MSG_SEND_INTERIOR = "queue-" + KEY_MSG_SEND_INTERIOR;
    /**
     * 站内信消费
     */
    @SneakyThrows
    @RabbitListener(queues = QUEUE_MSG_SEND_INTERIOR)
    public void timeLimit(Channel channel, org.springframework.amqp.core.Message message, String msg) {
        ArrayList<String> lists = new ArrayList<>();
        lists.add(msg);
        interior(lists);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
    }

    public void interior(List<String> messages) {
        messages.forEach(e -> {
            //判断类型， 处理消费
            Message message = JSONObject.parseObject(e, Message.class);
            send(message);
        });
    }

    public void send(Message message) {
        List recipients = message.getRecipients();
        if (ObjectNull.isNull(recipients)) {
            return;
        }
        List<PersonnelDto> personnelDtos = JSONArray.parseArray(JSONObject.toJSONString(recipients), PersonnelDto.class);
        List<String> userIds = personnelDtos.stream().map(e -> e.getId()).collect(Collectors.toList());
        switch (message.getSendType()) {
            case sms:
                //获取短信签名模板
                //当前这个租户的配置信息
                SysConfigSms config = sysConfigService.getConfig(ConfigsTypeEnum.SMS_CONFIGURATION);
                if (!config.getEnable()) {
                    throw new BusinessException("未配置短信");
                }
                // 获取短信签名
                String signName = config.getSignature();

                AliSmsDto dto = new AliSmsDto().setSignName(signName).setTemplateCode(message.getTemplateId()).setTemplateParam(JSONObject.toJSONString(message.getExtension()));
                List<ReceiversDto> smsMassage = userService.listByIds(userIds)
                        .stream()
                        .map(s -> new ReceiversDto().setUserName(s.getRealName()).setUserId(s.getId()).setReceiverConfig(s.getPhone()))
                        .collect(Collectors.toList());
                dto.setDefinedReceivers(smsMassage);
                aliSmsApi.sendAliSms(dto);
                break;
            case email:
                List<ReceiversDto> emailMessage = userService.listByIds(userIds)
                        .stream()
                        .map(s -> new ReceiversDto().setUserName(s.getRealName()).setUserId(s.getId()).setReceiverConfig(s.getEmail()))
                        .collect(Collectors.toList());
                EmailMessageDto baseMessage = new EmailMessageDto().setContent(message.getContent()).setTitle(message.getTitle());
                baseMessage.setDefinedReceivers(emailMessage).setClientCode(SpringContextUtil.getApplicationContextName());
                emailMessagePushApi.sendEmail(baseMessage);
                break;

            case DING_H5:
                //根据用户ID获取扩展ID
                List<ReceiversDto> receiversDtoList = userExtensionService.list(new LambdaQueryWrapper<UserExtension>()
                                .in(UserExtension::getUserId, userIds).eq(UserExtension::getType, "Ding"))
                        .stream()
                        .map(s -> new ReceiversDto().setUserId(s.getId()).setReceiverConfig(String.valueOf(s.getExtension().get("userid"))).setUserName(s.getNickname()))
                        .collect(Collectors.toList());
                TextMessageDTO dingMessage = new TextMessageDTO().setContent(message.getContent());
                dingMessage.setDefinedReceivers(receiversDtoList);
                dingTalkCorpApi.sendTextMessage(dingMessage);
                break;

            case interior:
                InsideNotificationDto interiorMessage = new InsideNotificationDto();
                //拼装数据
                Dict set = Dict.create().set("title", message.getTitle()).set("content", message.getContent());
                interiorMessage.setContent(JSONObject.toJSONString(set));
                List<ReceiversDto> receiversDtoList1 = userService.listByIds(userIds)
                        .stream()
                        .map(e -> new ReceiversDto().setUserId(e.getId()).setUserName(e.getRealName()).setTenantId(message.getTenantId()))
                        .collect(Collectors.toList());
                interiorMessage.setDefinedReceivers(receiversDtoList1);
                interiorMessage.setTenantId(message.getTenantId());
                insideNotificationApi.send(interiorMessage);
                break;
            case WX_ENTERPRISE:
                //企业微信
                //根据用户ID获取扩展ID
                List<ReceiversDto> wxReceiversDtoList = userExtensionService.list(new LambdaQueryWrapper<UserExtension>()
                                //企业微信，两种登录方式的openid都是一个
                                .in(UserExtension::getUserId, userIds).in(UserExtension::getType, "WX_ENTERPRISE"))
                        .stream()
                        .map(s -> new ReceiversDto().setUserId(s.getId()).setReceiverConfig(String.valueOf(s.getExtension().get("uuid"))).setUserName(s.getNickname()))
                        .collect(Collectors.toList());
                WeTextMessageDTO weTextMessageDTO = new WeTextMessageDTO();
                weTextMessageDTO.setContent(message.getContent());
                weTextMessageDTO.setDefinedReceivers(wxReceiversDtoList);
                wechatWorkAgentApi.sendWebChatTextMessage(weTextMessageDTO);
                break;

            case WECHAT_MP_MESSAGE:
                //公众号
                //根据用户ID获取扩展ID
                List<ReceiversDto> wxmpReceiversDtoList = userExtensionService.list(new LambdaQueryWrapper<UserExtension>()
                                .in(UserExtension::getUserId, userIds).eq(UserExtension::getType, LoginTypeEnum.WECHAT_MP))
                        .stream()
                        .map(s -> new ReceiversDto().setUserId(s.getUserId()).setReceiverConfig(s.getOpenId()).setUserName(s.getNickname()))
                        .collect(Collectors.toList());

                TemplateMessageDTO messageDto = new TemplateMessageDTO();
                messageDto.setWechatTemplateId(message.getTemplateId());
                messageDto.setDefinedReceivers(wxmpReceiversDtoList);
                List<WechatTemplateData> templateDataList = message.getExtension().keySet()
                        .stream()
                        .map(s -> new WechatTemplateData().setName(s).setValue(String.valueOf(message.getExtension().get(s))))
                        .collect(Collectors.toList());
                messageDto.setTemplateDataList(templateDataList);
                wechatOfficialAccountApi.sendWebChatTemplateMessage(messageDto);
                break;

            default:
                log.error("没有找到发送信息类型消息未发送", message.getId());
        }

    }
}
