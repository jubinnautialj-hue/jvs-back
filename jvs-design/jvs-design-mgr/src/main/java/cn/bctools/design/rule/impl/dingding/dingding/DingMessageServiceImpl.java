package cn.bctools.design.rule.impl.dingding.dingding;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.notice.dto.NoticeExtendTemplateDto;
import cn.bctools.design.notice.handler.enums.DingNoticeEnum;
import cn.bctools.design.notice.handler.send.DataNotifyDto;
import cn.bctools.design.notice.handler.send.impl.DingSendNotifyImpl;
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
@Rule(value = "发送钉钉消息",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.对象,
        order = 3,
        help = "https://open.dingtalk.com/document/orgapp/overview-3",
//        iconUrl = "rule-dingding",
        explain = "发送钉钉消息，只支持文本类型、图片类型、链接类型的信息。需要在系统设置中配置钉钉应用才能使用。"
)
@AllArgsConstructor
public class DingMessageServiceImpl implements BaseCustomFunctionInterface<DingMessageDto> {

    DingSendNotifyImpl dingSendNotify;

    @Override
    public Object execute(DingMessageDto dingMessageDto, Map<String, Object> params) {
        try {
            List<UserDto> userDtos = dingMessageDto.getUserIds().stream().map(userId -> new UserDto().setId(userId)).collect(Collectors.toList());
            NoticeExtendTemplateDto templateDto = new NoticeExtendTemplateDto();
            Map<DingNoticeEnum, Object> dingNotice = new HashMap<>(1);
            if (ObjectNull.isNull(dingMessageDto.getImgUrl())) {
                dingNotice.put(DingNoticeEnum.TEXT, "");
            } else {
                dingNotice.put(DingNoticeEnum.LinkMessage, "");
            }
            templateDto.setDing(dingNotice);
            dingSendNotify.send(new DataNotifyDto()
                    .setTenantId(TenantContextHolder.getTenantId())
                    .setUsers(userDtos)
                    .setTitle(dingMessageDto.getTitle())
                    .setImgUrl(dingMessageDto.getImgUrl())
                    .setMessageUrl(dingMessageDto.getMessageUrl())
                    .setContent(dingMessageDto.getContent())
                    .setTemplate(templateDto));
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("发送钉钉消息失败。exception：", e);
        }
        return Boolean.FALSE;
    }
}
