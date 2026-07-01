package cn.bctools.design.notice.handler.send.impl;

import cn.bctools.auth.api.api.AuthTenantConfigServiceApi;
import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigSms;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.design.notice.entity.enums.NoticeTypeEnum;
import cn.bctools.design.notice.handler.send.DataNotifyDto;
import cn.bctools.design.notice.handler.send.SendNotifyHandler;
import cn.bctools.message.push.api.AliSmsApi;
import cn.bctools.message.push.dto.messagepush.AliSmsDto;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 发送短信消息通知
 */
@Slf4j
@Component
@AllArgsConstructor
public class SmsSendNotifyImpl implements SendNotifyHandler {

    private final AliSmsApi api;
    private final AuthTenantConfigServiceApi sysConfigServiceApi;
    private final AuthUserServiceApi authUserServiceApi;

    @Override
    public String getType() {
        return NoticeTypeEnum.SMS.getValue();
    }

    @Override
    public void send(DataNotifyDto dto) {
        String str = sysConfigServiceApi.key(ConfigsTypeEnum.SMS_CONFIGURATION, dto.getTenantId()).getData();
        SysConfigSms data = sysConfigServiceApi.convertKey(ConfigsTypeEnum.SMS_CONFIGURATION, str);
        if (!data.getEnable()) {
            throw new BusinessException("未启用短信发送功能");
        }
        // 获取短信签名
        List<String> userIds = dto.getUsers().stream().map(UserDto::getId).collect(Collectors.toList());
        List<ReceiversDto> definedReceivers = authUserServiceApi.getByIds(userIds).getData()
                .stream()
                .map(s -> new ReceiversDto().setUserName(s.getRealName()).setUserId(s.getId()).setReceiverConfig(s.getPhone()))
                .collect(Collectors.toList());

        Map<String, String> variable = dto.getTemplateVariable().get(NoticeTypeEnum.SMS.getValue());
        AliSmsDto aliSmsDto = new AliSmsDto().setSignName(data.getSignature()).setTemplateCode(dto.getTemplate().getTemplateCode()).setTemplateParam(JSONObject.toJSONString(variable));
        aliSmsDto.setDefinedReceivers(definedReceivers);
        aliSmsDto.setClientCode(dto.getClientId());
        aliSmsDto.setTenantId(dto.getTenantId());
        log.debug("发送短信：{}", JSON.toJSONString(aliSmsDto));
        api.sendAliSms(aliSmsDto);
        log.info("发送短信完成");
    }
}
