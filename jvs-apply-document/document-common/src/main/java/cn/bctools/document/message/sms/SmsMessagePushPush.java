package cn.bctools.document.message.sms;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.MessageConfig;
import cn.bctools.document.entity.MessageSms;
import cn.bctools.document.enums.MessagePushTypeEnum;
import cn.bctools.document.message.MessagePushInterface;
import cn.bctools.document.service.MessageSmsService;
import cn.bctools.document.service.MessageVariableBinDingService;
import cn.bctools.document.vo.MessageVariableValueVo;
import cn.bctools.message.push.api.AliSmsApi;
import cn.bctools.message.push.dto.messagepush.AliSmsDto;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 阿里短信
 *
 * @author admin
 */
@Service("aliSms")
@AllArgsConstructor
public class SmsMessagePushPush implements MessagePushInterface<AliSmsDto> {
    private final AliSmsApi aliSmsApi;
    private final MessageSmsService messageSmsService;
    private final MessageVariableBinDingService messageVariableBinDingService;

    @Override
    public Boolean pushMessage(AliSmsDto message) {
        return aliSmsApi.sendAliSms(message).getData();
    }

    @Override
    public Boolean textPushMessage(String text, UserDto userDto) {
        return null;
    }

    @Override
    public AliSmsDto getData(MessageConfig messageConfig, DcLibrary dcLibrary, MessagePushTypeEnum operatorType, UserDto user) {
        MessageSms messageSms = messageSmsService.getById(messageConfig.getMessageId());
        AliSmsDto aliSmsDto = new AliSmsDto();
        List<ReceiversDto> receiversDto = messageConfig.getUserIds().stream().map(e -> new ReceiversDto().setUserId(e)).collect(Collectors.toList());
        //获取变量
        List<MessageVariableValueVo> list = messageVariableBinDingService.getVariable(messageConfig.getMessageId(), dcLibrary.getId(), user);
        //解析变量
        Map<String, String> map = new HashMap<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            MessageVariableValueVo messageVariableValueVo = list.get(i);
            map.put(messageVariableValueVo.getName(), messageVariableValueVo.getValue());
        }
        aliSmsDto.setSignName(messageSms.getSignName())
                .setSmsUpExtendCode(messageSms.getSmsUpExtendCode())
                .setTemplateCode(messageSms.getTemplateCode())
                .setTemplateParam(JSONObject.toJSONString(map))
                .setDefinedReceivers(receiversDto);
        return aliSmsDto;
    }

}
