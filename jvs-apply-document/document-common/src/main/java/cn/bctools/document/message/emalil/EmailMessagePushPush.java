package cn.bctools.document.message.emalil;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.MessageConfig;
import cn.bctools.document.entity.MessageEmail;
import cn.bctools.document.enums.MessagePushTypeEnum;
import cn.bctools.document.message.MessagePushInterface;
import cn.bctools.document.service.MessageEmailService;
import cn.bctools.document.service.MessageVariableBinDingService;
import cn.bctools.document.vo.MessageVariableValueVo;
import cn.bctools.message.push.api.EmailMessagePushApi;
import cn.bctools.message.push.dto.messagepush.EmailMessageDto;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("email")
@AllArgsConstructor
public class EmailMessagePushPush implements MessagePushInterface<EmailMessageDto> {
    private final EmailMessagePushApi emailMessagePushApi;
    private final MessageEmailService messageEmailService;
    private final MessageVariableBinDingService messageVariableBinDingService;

    @Override
    public Boolean textPushMessage(String text, UserDto userDto) {
        return null;
    }

    @Override
    public Boolean pushMessage(EmailMessageDto message) {
        return emailMessagePushApi.sendEmail(message).getData();
    }

    @Override
    public EmailMessageDto getData(MessageConfig messageConfig, DcLibrary dcLibrary, MessagePushTypeEnum operatorType, UserDto user) {
        EmailMessageDto emailMessageDto = new EmailMessageDto();
        List<ReceiversDto> receiversDto = messageConfig.getUserIds().stream().map(e -> new ReceiversDto().setUserId(e)).collect(Collectors.toList());
        List<MessageVariableValueVo> variable = messageVariableBinDingService.getVariable(messageConfig.getMessageId(), dcLibrary.getId(), user);
        MessageEmail messageEmail = messageEmailService.getById(messageConfig.getMessageId());
        String content = messageEmail.getContent();
        //替换值
        for (int i = 0; i < variable.size(); i++) {
            MessageVariableValueVo messageVariableValueVo = variable.get(i);
            content = content.replaceAll("\\$\\{" + messageVariableValueVo.getName() + "\\}", String.valueOf(messageVariableValueVo.getValue()));
        }
        emailMessageDto
                .setContent(content)
                .setDefinedReceivers(receiversDto);
        return emailMessageDto;
    }
}
