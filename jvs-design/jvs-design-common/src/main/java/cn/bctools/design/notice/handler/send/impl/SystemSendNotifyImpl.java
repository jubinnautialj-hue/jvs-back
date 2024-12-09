package cn.bctools.design.notice.handler.send.impl;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.notice.entity.enums.NoticeTypeEnum;
import cn.bctools.design.notice.handler.send.DataNotifyDto;
import cn.bctools.design.notice.handler.send.SendNotifyHandler;
import cn.bctools.message.push.api.InsideNotificationApi;
import cn.bctools.message.push.dto.messagepush.InsideNotificationDto;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 发送站内信消息通知
 */
@Slf4j
@Component
@AllArgsConstructor
public class SystemSendNotifyImpl implements SendNotifyHandler {

    private final InsideNotificationApi api;

    @Override
    public String getType() {
        return NoticeTypeEnum.SYSTEM.getValue();
    }

    @Override
    public void send(DataNotifyDto dto) {
        InsideNotificationDto interiorMessage = new InsideNotificationDto();
        //拼装数据
        Dict set = Dict.create().set("title", dto.getTitle()).set("content", dto.getContent());
        if (ObjectNull.isNotNull(dto.getNoticeDataExtend())) {
            set.parseBean(dto.getNoticeDataExtend());
        }
        interiorMessage.setContent(JSONObject.toJSONString(set));
        interiorMessage.setClientCode(dto.getClientId());
        interiorMessage.setTenantId(dto.getTenantId());
        List<ReceiversDto> definedReceivers = dto.getUsers().stream().map(u -> new ReceiversDto().setUserId(u.getId()).setUserName(u.getRealName())).collect(Collectors.toList());
        interiorMessage.setDefinedReceivers(definedReceivers);
        api.send(interiorMessage);
        log.info("发送站内信消息完成");
    }

}
