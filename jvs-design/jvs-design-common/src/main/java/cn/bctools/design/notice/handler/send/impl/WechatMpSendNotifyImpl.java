package cn.bctools.design.notice.handler.send.impl;

import cn.bctools.auth.api.api.UserExtensionServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.enums.OtherLoginTypeEnum;
import cn.bctools.design.notice.entity.enums.NoticeTypeEnum;
import cn.bctools.design.notice.handler.send.DataNotifyDto;
import cn.bctools.design.notice.handler.send.SendNotifyHandler;
import cn.bctools.message.push.api.WechatOfficialAccountApi;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import cn.bctools.message.push.dto.messagepush.wechatofficialaccount.TemplateMessageDTO;
import cn.bctools.message.push.dto.messagepush.wechatofficialaccount.WechatTemplateData;
import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 发送公众号消息通知
 */
@Slf4j
@Component
@AllArgsConstructor
public class WechatMpSendNotifyImpl implements SendNotifyHandler {
    private final WechatOfficialAccountApi api;
    private final UserExtensionServiceApi userExtensionServiceApi;

    @Override
    public String getType() {
        return NoticeTypeEnum.WECHAT_MP.getValue();
    }

    @Override
    public void send(DataNotifyDto dto) {
        TemplateMessageDTO templateMessage = dto.getTemplate().getWechatMp();
        Map<String, String> variable = dto.getTemplateVariable().get(NoticeTypeEnum.WECHAT_MP.getValue());
        List<WechatTemplateData> templateDataList;
        if (MapUtils.isNotEmpty(variable)) {
            templateDataList = templateMessage.getTemplateDataList().stream().map(t -> t.setValue(variable.get(t.getName()))).collect(Collectors.toList());
            templateMessage.setTemplateDataList(templateDataList);
        }
        // 封装消息
        List<String> userIds = dto.getUsers().stream().map(UserDto::getId).collect(Collectors.toList());
        List<ReceiversDto> definedReceivers = Optional.ofNullable(userExtensionServiceApi.query(userIds, OtherLoginTypeEnum.WECHAT_MP.name()).getData()).orElse(new ArrayList<>())
                .stream()
                .map(s -> new ReceiversDto().setUserId(s.getUserId()).setReceiverConfig(s.getOpenId()).setUserName(s.getNickname()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(definedReceivers)) {
            log.warn("发送微信公众号消息失败: 未找到用户");
            return;
        }
        templateMessage.setDefinedReceivers(definedReceivers);
        templateMessage.setWechatTemplateId(dto.getTemplate().getTemplateCode());
        templateMessage.setClientCode(dto.getClientId());
        templateMessage.setTenantId(dto.getTenantId());
        log.debug("发送微信公众号消息：{}", JSON.toJSONString(templateMessage));
        // 发送公共号消息
        api.sendWebChatTemplateMessage(templateMessage);
        log.info("发送微信公众号消息完成");
    }
}
