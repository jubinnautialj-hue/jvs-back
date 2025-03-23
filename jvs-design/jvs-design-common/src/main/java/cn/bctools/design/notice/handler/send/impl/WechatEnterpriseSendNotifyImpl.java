package cn.bctools.design.notice.handler.send.impl;

import cn.bctools.auth.api.api.UserExtensionServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.enums.OtherLoginTypeEnum;
import cn.bctools.design.notice.entity.enums.NoticeTypeEnum;
import cn.bctools.design.notice.handler.enums.WxEnterpriseNoticeEnum;
import cn.bctools.design.notice.handler.send.DataNotifyDto;
import cn.bctools.design.notice.handler.send.SendNotifyHandler;
import cn.bctools.message.push.api.WechatWorkAgentApi;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import cn.bctools.message.push.dto.messagepush.wechatwork.agent.WeTextMessageDTO;
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
 * 发送企业微信消息通知
 */
@Slf4j
@Component
@AllArgsConstructor
public class WechatEnterpriseSendNotifyImpl implements SendNotifyHandler {

    private final WechatWorkAgentApi api;
    private final UserExtensionServiceApi userExtensionServiceApi;

    @Override
    public String getType() {
        return NoticeTypeEnum.WECHAT_ENTERPRISE.getValue();
    }

    @Override
    public void send(DataNotifyDto dto) {
        //根据用户ID获取扩展ID
        List<String> userIds = dto.getUsers().stream().map(UserDto::getId).collect(Collectors.toList());
        List<ReceiversDto> definedReceivers = Optional.ofNullable(userExtensionServiceApi.query(userIds, OtherLoginTypeEnum.WX_ENTERPRISE.name()).getData()).orElse(new ArrayList<>())
                .stream()
                .map(s -> new ReceiversDto().setUserId(s.getUserId()).setReceiverConfig(String.valueOf(s.getOpenId())).setUserName(s.getNickname()))
                        .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(definedReceivers)) {
            log.warn("发送企业微信消息失败: 未找到用户");
            return;
        }
        Map<WxEnterpriseNoticeEnum, Object> wxEnterprise = dto.getTemplate().getWxEnterprise();
        wxEnterprise.entrySet().parallelStream().forEach(w -> {
            if (WxEnterpriseNoticeEnum.TEXT.equals(w.getKey())) {
                sendText(dto.getTenantId(), dto.getClientId(), definedReceivers, dto.getContent());
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
        WeTextMessageDTO weTextMessageDTO = new WeTextMessageDTO();
        weTextMessageDTO.setContent(content);
        weTextMessageDTO.setDefinedReceivers(definedReceivers);
        weTextMessageDTO.setClientCode(clientId);
        weTextMessageDTO.setTenantId(tenantId);
        log.debug("发送企业微信文本消息：{}", JSON.toJSONString(weTextMessageDTO));
        api.sendWebChatTextMessage(weTextMessageDTO);
        log.info("发送企业微信文本消息完成");
    }
}
