package cn.bctools.rule.tools.message;


import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.message.push.api.InsideNotificationApi;
import cn.bctools.message.push.dto.messagepush.InsideNotificationDto;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Slf4j
@Rule(value = "站内信",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.未识别,
        testShowEnum = TestShowEnum.JSON,
        order = 24,
//        iconUrl = "rule-Messages",
        explain = "给指定用户发送站内消息。"
)
public class MessageServiceImpl implements BaseCustomFunctionInterface<MessageDto> {

    @Autowired
    InsideNotificationApi insideNotificationApi;

    @Override
    public Object execute(MessageDto messageDto, Map<String, Object> params) {
        InsideNotificationDto interiorMessage = new InsideNotificationDto();
        //拼装数据
        Dict set = Dict.create().set("title", messageDto.getTitle()).set("content", messageDto.getContent());
        interiorMessage.setContent(JSONObject.toJSONString(set));
        List<ReceiversDto> definedReceivers = messageDto.getUserIds().stream().map(u -> new ReceiversDto().setUserId(u)).collect(Collectors.toList());
        interiorMessage.setDefinedReceivers(definedReceivers);
        interiorMessage.setTenantId(TenantContextHolder.getTenantId());
        insideNotificationApi.send(interiorMessage);
        return "ok";
    }

}
