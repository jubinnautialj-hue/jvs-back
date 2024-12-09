package cn.bctools.design.rule.impl.wechat;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.notice.dto.NoticeExtendTemplateDto;
import cn.bctools.design.notice.handler.enums.WxEnterpriseNoticeEnum;
import cn.bctools.design.notice.handler.send.DataNotifyDto;
import cn.bctools.design.notice.handler.send.impl.WechatEnterpriseSendNotifyImpl;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Rule(value = "企业微信消息",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.对象,
        order = 6,
//        iconUrl = "rule-Messages",
        explain = "给指定用户发送企业微信消息."
)
@AllArgsConstructor
public class WechaatEnterpriseServiceImpl implements BaseCustomFunctionInterface<WechatEnterpriseFunctionDto> {

    WechatEnterpriseSendNotifyImpl wechatEnterpriseSendNotify;
    @Override
    public Object execute(WechatEnterpriseFunctionDto messageDto, Map<String, Object> params) {
        try {
            List<UserDto> userDtos =  messageDto.getUserIds().stream().map(userId -> new UserDto().setId(userId)).collect(Collectors.toList());
            NoticeExtendTemplateDto templateDto = new NoticeExtendTemplateDto();
            Map<WxEnterpriseNoticeEnum, Object> wxEnterpriseNoticeEnumObjectMap = new HashMap<>(1);
            wxEnterpriseNoticeEnumObjectMap.put(WxEnterpriseNoticeEnum.TEXT, "");
            templateDto.setWxEnterprise(wxEnterpriseNoticeEnumObjectMap);
            wechatEnterpriseSendNotify.send(new DataNotifyDto()
                    .setTenantId(TenantContextHolder.getTenantId())
                    .setUsers(userDtos)
                    .setContent(messageDto.getContent())
                    .setTemplate(templateDto));
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("发送钉钉消息失败。exception：", e);
        }
        return Boolean.FALSE;
    }
}
