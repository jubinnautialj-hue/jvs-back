package cn.bctools.document.message.inside;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.MessageConfig;
import cn.bctools.document.entity.MessageInside;
import cn.bctools.document.enums.MessagePushTypeEnum;
import cn.bctools.document.message.MessagePushInterface;
import cn.bctools.document.service.MessageInsideService;
import cn.bctools.document.service.MessageVariableBinDingService;
import cn.bctools.document.vo.MessageVariableValueVo;
import cn.bctools.message.push.api.InsideNotificationApi;
import cn.bctools.message.push.dto.messagepush.InsideNotificationDto;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service("inside")
@AllArgsConstructor
@Slf4j
public class InsideMessagePushPush implements MessagePushInterface<InsideNotificationDto> {
    private final InsideNotificationApi insideNotificationApi;
    private final MessageVariableBinDingService messageVariableBinDingService;
    private final MessageInsideService messageInsideService;

    @Override
    public Boolean pushMessage(InsideNotificationDto message) {
        return insideNotificationApi.send(message).getData();
    }

    @Override
    public Boolean textPushMessage(String text, UserDto userDto) {
        String msg = SpringContextUtil.msg("文档通知");
        Dict set = Dict.create().set("title", msg).set("content", text);
        ReceiversDto receiversDto = new ReceiversDto().setUserId(userDto.getId()).setTenantId(userDto.getTenantId());
        InsideNotificationDto notificationDto = new InsideNotificationDto();
        notificationDto
                .setContent(JSONObject.toJSONString(set))
                .setDefinedReceivers(Arrays.asList(receiversDto));
        log.info("通知消息:{}", msg);
        return this.pushMessage(notificationDto);
    }

    @Override
    public InsideNotificationDto getData(MessageConfig messageConfig, DcLibrary dcLibrary, MessagePushTypeEnum operatorType, UserDto user) {
        InsideNotificationDto notificationDto = new InsideNotificationDto();
        List<ReceiversDto> receiversDto = messageConfig.getUserIds().stream().map(e -> new ReceiversDto().setUserId(e)).collect(Collectors.toList());
        List<MessageVariableValueVo> variable = messageVariableBinDingService.getVariable(messageConfig.getMessageId(), dcLibrary.getId(), user);
        MessageInside byId = messageInsideService.getById(messageConfig.getMessageId());
        String content = byId.getContent();
        //替换值
        for (int i = 0; i < variable.size(); i++) {
            MessageVariableValueVo messageVariableValueVo = variable.get(i);
            content = content.replaceAll("\\$\\{" + messageVariableValueVo.getName() + "\\}", String.valueOf(messageVariableValueVo.getValue()));
        }
        //拼装数据
        Dict set = Dict.create().set("title", operatorType.title).set("content", content);
        notificationDto
                .setContent(JSONObject.toJSONString(set))
                .setDefinedReceivers(receiversDto);
        return notificationDto;
    }
}
