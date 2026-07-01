package cn.bctools.design.notice.handler.send.impl;

import cn.bctools.auth.api.api.UserExtensionServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.enums.OtherLoginTypeEnum;
import cn.bctools.design.notice.entity.enums.NoticeTypeEnum;
import cn.bctools.design.notice.handler.enums.DingNoticeEnum;
import cn.bctools.design.notice.handler.send.DataNotifyDto;
import cn.bctools.design.notice.handler.send.SendNotifyHandler;
import cn.bctools.message.push.api.DingTalkCorpApi;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import cn.bctools.message.push.dto.messagepush.dingtalk.corp.LinkMessageDTO;
import cn.bctools.message.push.dto.messagepush.dingtalk.corp.TextMessageDTO;
import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 发送钉钉消息通知
 */
@Slf4j
@Component
@AllArgsConstructor
public class DingSendNotifyImpl implements SendNotifyHandler {
    private final DingTalkCorpApi api;
    private final UserExtensionServiceApi userExtensionServiceApi;

    @Override
    public String getType() {
        return NoticeTypeEnum.DING.getValue();
    }

    @Override
    public void send(DataNotifyDto dto) {
        //根据用户ID获取扩展ID
        List<String> userIds = dto.getUsers().stream().map(UserDto::getId).collect(Collectors.toList());
        List<ReceiversDto> definedReceivers = Optional.ofNullable(userExtensionServiceApi.query(userIds, OtherLoginTypeEnum.Ding.name()).getData()).orElse(new ArrayList<>())
                .stream()
                .map(s -> new ReceiversDto().setUserId(s.getId()).setReceiverConfig(String.valueOf(s.getExtension().get("userid"))).setUserName(s.getNickname()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(definedReceivers)) {
            log.warn("发送钉钉消息失败: 未找到用户");
            return;
        }
        Map<DingNoticeEnum, Object> dingNotice = dto.getTemplate().getDing();
        dingNotice.entrySet().parallelStream().forEach(w -> {
            switch (w.getKey()) {
                case TEXT:
                    sendText(dto.getTenantId(), dto.getClientId(), definedReceivers, dto.getContent());
                    break;
                case LinkMessage:
                    sendLinkMessage(dto.getTenantId(), dto.getClientId(), definedReceivers, dto.getTitle(), dto.getImgUrl(), dto.getContent(), dto.getMessageUrl());
                    break;
                default:
            }

        });
    }


    /**
     * 发送文本消息
     *
     * @param tenantId
     * @param clientId
     * @param definedReceivers
     * @param content
     */
    private void sendText(String tenantId, String clientId, List<ReceiversDto> definedReceivers, String content) {
        TextMessageDTO dingMessage = new TextMessageDTO().setContent(content);
        dingMessage.setDefinedReceivers(definedReceivers);
        dingMessage.setClientCode(clientId);
        dingMessage.setTenantId(tenantId);
        log.debug("发送钉钉文本消息：{}", JSON.toJSONString(dingMessage));
        api.sendTextMessage(dingMessage);
        log.info("发送钉钉文本消息完成");
    }

    private void sendLinkMessage(String tenantId, String clientId, List<ReceiversDto> definedReceivers, String title, String url, String content, String messageUrl) {
        LinkMessageDTO dto = new LinkMessageDTO();
        dto.setPicUrl(url);
        dto.setTitle(title);
        dto.setClientCode(clientId);
        dto.setText(content);
        dto.setMessageUrl(messageUrl);
        dto.setDefinedReceivers(definedReceivers);
        dto.setTenantId(tenantId);
        log.debug("发送钉钉消息：{}", JSON.toJSONString(dto));
        api.sendLinkMessage(dto);
        log.info("发送钉钉消息完成");
    }

}
