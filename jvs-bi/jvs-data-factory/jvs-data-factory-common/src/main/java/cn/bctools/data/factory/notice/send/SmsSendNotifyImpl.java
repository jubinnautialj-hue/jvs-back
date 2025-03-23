package cn.bctools.data.factory.notice.send;

import cn.bctools.auth.api.api.AuthTenantConfigServiceApi;
import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigSms;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.notice.dto.DataNotifyDto;
import cn.bctools.data.factory.notice.enums.NoticeTypeEnum;
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
 * @Author: ZhuXiaoKang
 * @Description: 发送短信消息通知
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
        List<ReceiversDto> definedReceivers = dto.getUsers()
                .stream()
                .map(s -> new ReceiversDto().setUserName(s.getRealName()).setUserId(s.getId()).setReceiverConfig(s.getPhone()))
                .collect(Collectors.toList());

        Map<String, String> variable = dto.getTemplateVariable().get(NoticeTypeEnum.SMS.getValue());
        AliSmsDto aliSmsDto = new AliSmsDto().setSignName(data.getSignature()).setTemplateCode(dto.getTemplate().getTemplateCode()).setTemplateParam(JSONObject.toJSONString(variable));
        aliSmsDto.setTenantId(dto.getTenantId());
        aliSmsDto.setDefinedReceivers(definedReceivers);
        aliSmsDto.setClientCode(dto.getClientId());
        log.debug("发送短信：{}", JSON.toJSONString(aliSmsDto));
        api.sendAliSms(aliSmsDto);
        log.info("发送短信完成");
    }
}
