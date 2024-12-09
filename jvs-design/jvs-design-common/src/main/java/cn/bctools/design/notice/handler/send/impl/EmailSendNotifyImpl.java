package cn.bctools.design.notice.handler.send.impl;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.notice.entity.enums.NoticeTypeEnum;
import cn.bctools.design.notice.handler.send.DataNotifyDto;
import cn.bctools.design.notice.handler.send.SendNotifyHandler;
import cn.bctools.message.push.api.EmailMessagePushApi;
import cn.bctools.message.push.dto.messagepush.EmailMessageDto;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 发送邮件消息通知
 */
@Slf4j
@Component
@AllArgsConstructor
public class EmailSendNotifyImpl implements SendNotifyHandler {

    private final EmailMessagePushApi api;
    private final AuthUserServiceApi authUserServiceApi;

    @Override
    public String getType() {
        return NoticeTypeEnum.EMAIL.getValue();
    }

    @Override
    public void send(DataNotifyDto dto) {
        List<String> userIds = dto.getUsers().stream().map(UserDto::getId).collect(Collectors.toList());
        List<ReceiversDto> definedReceivers = authUserServiceApi.getByIds(userIds).getData()
                .stream()
                .map(s -> new ReceiversDto().setUserName(s.getRealName()).setUserId(s.getId()).setReceiverConfig(s.getEmail()))
                .collect(Collectors.toList());
        EmailMessageDto baseMessage = new EmailMessageDto().setContent(dto.getContent()).setTitle(dto.getTitle());
        baseMessage.setDefinedReceivers(definedReceivers);
        baseMessage.setClientCode(dto.getClientId());
        baseMessage.setTenantId(dto.getTenantId());
        log.debug("发送邮件：{}", JSON.toJSONString(baseMessage));
        api.sendEmail(baseMessage);
        log.info("发送邮件完成");
    }
}
