package cn.bctools.data.factory.notice.send;

import cn.bctools.data.factory.notice.dto.DataNotifyDto;
import cn.bctools.data.factory.notice.enums.NoticeTypeEnum;
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
 * @Author: ZhuXiaoKang
 * @Description: 发送邮件消息通知
 */
@Slf4j
@Component
@AllArgsConstructor
public class EmailSendNotifyImpl implements SendNotifyHandler {

    private final EmailMessagePushApi api;

    @Override
    public String getType() {
        return NoticeTypeEnum.EMAIL.getValue();
    }

    @Override
    public void send(DataNotifyDto dto) {
        List<ReceiversDto> definedReceivers = dto.getUsers()
                .stream()
                .map(s -> new ReceiversDto().setUserName(s.getRealName()).setUserId(s.getId()).setReceiverConfig(s.getEmail()))
                .collect(Collectors.toList());
        EmailMessageDto baseMessage = new EmailMessageDto().setContent(dto.getContent()).setTitle(dto.getTitle());
        baseMessage.setTenantId(dto.getTenantId());
        baseMessage.setDefinedReceivers(definedReceivers);
        baseMessage.setClientCode(dto.getClientId());
        log.debug("发送邮件：{}", JSON.toJSONString(baseMessage));
        api.sendEmail(baseMessage);
        log.info("发送邮件完成");
    }
}
