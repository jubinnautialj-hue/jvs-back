package cn.bctools.message.push.handler.dingtalk.corp;

import cn.bctools.auth.api.api.AuthTenantConfigServiceApi;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigDing;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.message.push.dto.enums.MessagePushStatusEnum;
import cn.bctools.message.push.dto.enums.MessageTypeEnum;
import cn.bctools.message.push.dto.enums.PlatformEnum;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import cn.bctools.message.push.dto.messagepush.dingtalk.corp.ActionCardMultiMessageDTO;
import cn.bctools.message.push.dto.messagepush.dingtalk.corp.BtnJsonDTO;
import cn.bctools.message.push.entity.MessagePushHis;
import cn.bctools.message.push.handler.BaseMessageHandler;
import cn.bctools.message.push.service.MessagePushHisService;
import cn.bctools.message.push.utils.SingletonUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson2.JSON;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiMessageCorpconversationAsyncsendV2Request;
import com.dingtalk.api.response.OapiMessageCorpconversationAsyncsendV2Response;
import com.taobao.api.ApiException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 钉钉工作通知文本类型消息处理器
 *
 * @author wl*/
@Slf4j
@Component
@AllArgsConstructor
public class CorpActionCardMultiMessageHandler extends BaseMessageHandler<ActionCardMultiMessageDTO> {

    private final MessagePushHisService messagePushHisService;
    private final AuthTenantConfigServiceApi configServiceApi;

    @Override
    public void handle(ActionCardMultiMessageDTO param) {
        if (!param.hasReceiver()) {
            throw new BusinessException("没有检测到接收人配置");
        }
        String body = configServiceApi.key(ConfigsTypeEnum.NAIL_APPLICATION_CONFIGURATION, TenantContextHolder.getTenantId()).getData();
        SysConfigDing config = configServiceApi.convertKey(ConfigsTypeEnum.NAIL_APPLICATION_CONFIGURATION, body);
        if (!config.getEnable()) {
            throw new IllegalArgumentException("未找到钉钉配置，请核实!");
        }

        //获取接收人
        List<String> receiverUsers = param.getDefinedReceivers().stream().map(ReceiversDto::getReceiverConfig).collect(Collectors.toList());

        DingTalkClient dingTalkClient = SingletonUtil.get("dinging-" + config.getAppKey() + config.getAppSecret(),
                (SingletonUtil.Factory<DingTalkClient>) () -> new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2"));
        OapiMessageCorpconversationAsyncsendV2Request request = new OapiMessageCorpconversationAsyncsendV2Request();
        request.setAgentId(Long.valueOf(config.getAgentId()));
        request.setUseridList(String.join(",", receiverUsers));
        request.setDeptIdList(param.getDeptIdList());
        request.setToAllUser(param.getToAllUser());

        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
        msg.setActionCard(new OapiMessageCorpconversationAsyncsendV2Request.ActionCard());
        msg.getActionCard().setTitle(param.getTitle());
        msg.getActionCard().setSingleTitle(param.getTitle());
        msg.getActionCard().setMarkdown(param.getMarkdown());
        msg.getActionCard().setBtnOrientation(param.getBtnOrientation());
        List<OapiMessageCorpconversationAsyncsendV2Request.BtnJsonList> btnJsonList = new ArrayList<>();
        for (BtnJsonDTO btnJsonDTO : param.getBtnJsonList()) {
            OapiMessageCorpconversationAsyncsendV2Request.BtnJsonList btnJson = new OapiMessageCorpconversationAsyncsendV2Request.BtnJsonList();
            btnJson.setActionUrl(btnJsonDTO.getActionUrl());
            btnJson.setTitle(btnJsonDTO.getTitle());
            btnJsonList.add(btnJson);
        }
        msg.getActionCard().setBtnJsonList(btnJsonList);
        msg.setMsgtype("action_card");
        request.setMsg(msg);

        //生成批次号
        String batchNumber = UUID.randomUUID().toString().replaceAll("-", "");

        MessagePushHis messagePushHis = new MessagePushHis()
                .setBatchNumber(batchNumber)
                .setMessageContent(JSON.toJSONString(param))
                .setPlatform(PlatformEnum.DING_TALK_CORP)
                .setMessageType(MessageTypeEnum.DING_TALK_COPR_ACTION_CARD_MULTI);

        try {
            OapiMessageCorpconversationAsyncsendV2Response rsp = dingTalkClient.execute(request, AccessTokenUtils.getAccessToken(config.getAppKey(), config.getAppSecret()));
            if (!rsp.isSuccess()) {
                throw new IllegalStateException(rsp.getBody());
            }
            messagePushHis.setPushStatus(MessagePushStatusEnum.SUCCESS);
        } catch (ApiException e) {
            log.error("消息发送异常",e);
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
        ActionCardMultiMessageDTO dto = JSON.parseObject(his.getMessageContent(), ActionCardMultiMessageDTO.class);
        dto.getDefinedReceivers().removeIf(e -> e.getUserId() == null || !e.getUserId().equals(his.getUserId()));
        handle(dto);
    }
}
