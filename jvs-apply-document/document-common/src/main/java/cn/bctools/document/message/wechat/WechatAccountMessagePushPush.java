package cn.bctools.document.message.wechat;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.MessageConfig;
import cn.bctools.document.entity.MessageWechat;
import cn.bctools.document.enums.MessagePushTypeEnum;
import cn.bctools.document.message.MessagePushInterface;
import cn.bctools.document.service.MessageVariableBinDingService;
import cn.bctools.document.service.MessageWechatService;
import cn.bctools.document.vo.MessageVariableValueVo;
import cn.bctools.message.push.api.WechatOfficialAccountApi;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import cn.bctools.message.push.dto.messagepush.wechatofficialaccount.TemplateMessageDTO;
import cn.bctools.message.push.dto.messagepush.wechatofficialaccount.WechatTemplateData;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 微信公众号模板消息
 *
 * @author adnub
 */
@Service("accountTemplate")
@AllArgsConstructor
public class WechatAccountMessagePushPush implements MessagePushInterface<TemplateMessageDTO> {
    private final WechatOfficialAccountApi wechatOfficialAccountApi;
    private final MessageWechatService messageWechatService;
    private final MessageVariableBinDingService messageVariableBinDingService;

    @Override
    public Boolean textPushMessage(String text, UserDto userDto) {
        return null;
    }

    @Override
    public Boolean pushMessage(TemplateMessageDTO message) {
        return wechatOfficialAccountApi.sendWebChatTemplateMessage(message).getData();
    }

    @Override
    public TemplateMessageDTO getData(MessageConfig messageConfig, DcLibrary dcLibrary, MessagePushTypeEnum operatorType, UserDto user) {
        MessageWechat byId = messageWechatService.getById(messageConfig.getMessageId());
        TemplateMessageDTO templateMessageDTO = new TemplateMessageDTO();
        List<ReceiversDto> receiversDto = messageConfig.getUserIds().stream().map(e -> new ReceiversDto().setUserId(e)).collect(Collectors.toList());
        //获取变量
        List<MessageVariableValueVo> list = messageVariableBinDingService.getVariable(messageConfig.getMessageId(), dcLibrary.getId(), user);
        List<WechatTemplateData> templateData = list.parallelStream().map(e ->
                new WechatTemplateData()
                        .setColor(e.getColor())
                        .setName(e.getName())
                        .setValue(e.getValue())
        ).collect(Collectors.toList());
        templateMessageDTO.setWechatTemplateId(byId.getWechatTemplateId())
                .setTemplateDataList(templateData)
                .setDefinedReceivers(receiversDto);
        return templateMessageDTO;
    }
}
