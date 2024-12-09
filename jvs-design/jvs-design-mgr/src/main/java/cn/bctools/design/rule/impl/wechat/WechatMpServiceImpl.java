package cn.bctools.design.rule.impl.wechat;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.notice.dto.NoticeExtendTemplateDto;
import cn.bctools.design.notice.entity.enums.NoticeTypeEnum;
import cn.bctools.design.notice.handler.send.DataNotifyDto;
import cn.bctools.design.notice.handler.send.impl.WechatMpSendNotifyImpl;
import cn.bctools.message.push.dto.messagepush.wechatofficialaccount.TemplateMessageDTO;
import cn.bctools.message.push.dto.messagepush.wechatofficialaccount.WechatTemplateData;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Rule(value = "公众号消息",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.数组,
        testShowEnum = TestShowEnum.TEXT,
        order = 7,
//        iconUrl = "rule-Messages",
        explain = "给指定用户发送选择的模板消息。"
)
@AllArgsConstructor
public class WechatMpServiceImpl implements BaseCustomFunctionInterface<WechatMpFunctionDto> {

    WechatMpSendNotifyImpl wechatMpSendNotify;

    @Override
    public Object execute(WechatMpFunctionDto wechatMpFunctionDto, Map<String, Object> params) {
        NoticeExtendTemplateDto templateDto = new NoticeExtendTemplateDto();
        templateDto.setTemplateCode(wechatMpFunctionDto.getMessageTemplateCode());
        List<WechatTemplateData> list = wechatMpFunctionDto.getBody().keySet().stream().map(e -> new WechatTemplateData().setValue(e).setName(e)).collect(Collectors.toList());
        templateDto.setWechatMp(new TemplateMessageDTO().setTemplateDataList(list).setWechatTemplateId(wechatMpFunctionDto.getMessageTemplateCode()));
        HashMap<String, Map<String, String>> map = new HashMap<>(1);
        map.put(NoticeTypeEnum.WECHAT_MP.getValue(), wechatMpFunctionDto.getBody());
        List<UserDto> userDtos = wechatMpFunctionDto.getUserIds().stream().map(userId -> new UserDto().setId(userId)).collect(Collectors.toList());
        wechatMpSendNotify.send(new DataNotifyDto()
                .setTenantId(TenantContextHolder.getTenantId())
                .setUsers(userDtos)
                .setTemplateVariable(map)
                .setTemplate(templateDto));
        return Boolean.TRUE;
    }
}
