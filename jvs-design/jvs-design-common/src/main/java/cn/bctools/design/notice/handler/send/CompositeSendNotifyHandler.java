package cn.bctools.design.notice.handler.send;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.design.notice.dto.NoticeExtendTemplateDto;
import cn.bctools.design.notice.dto.QueryDataNoticeRespDto;
import cn.bctools.design.notice.entity.enums.NoticeTypeEnum;
import cn.bctools.design.notice.handler.bo.NoticeDataExtendBo;
import cn.bctools.design.notice.handler.util.NoticeVariableUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 各平台发送消息入口
 */
@Slf4j
@Component
public class CompositeSendNotifyHandler {

    private final Map<String, SendNotifyHandler> flowHandlerMap;
    private final AuthUserServiceApi authUserServiceApi;

    @Autowired
    public CompositeSendNotifyHandler(List<SendNotifyHandler> sendNotifyHandlers, AuthUserServiceApi authUserServiceApi) {
        this.flowHandlerMap = sendNotifyHandlers.stream().collect(Collectors.toMap(SendNotifyHandler::getType, Function.identity()));
        this.authUserServiceApi = authUserServiceApi;
    }

    /**
     * 解析消息通知配置并发送
     *
     * @param tenantId         租户id
     * @param dataNotice       通知配置
     * @param data             数据
     * @param userIds          接收用户
     * @param noticeDataExtend 消息数据扩展
     */
    @Async
    public void send(String tenantId, QueryDataNoticeRespDto dataNotice, Map<String, Object> data, List<String> userIds, NoticeDataExtendBo noticeDataExtend) {
        // 获取用户信息
        if (ObjectNull.isNull(userIds)) {
            return;
        }
        List<UserDto> users = authUserServiceApi.getByIds(userIds).getData();
        // 消息模板扩展配置
        List<NoticeExtendTemplateDto> extendDtos = dataNotice.getExtend();
        // 兼容2.1.5
        if (ObjectNull.isNull(extendDtos)) {
            // 消息标题
            String title = NoticeVariableUtils.replacementText(dataNotice.getTitleHtml(), data);
            // 消息通知内容
            String content = NoticeVariableUtils.replacement(NoticeTypeEnum.SYSTEM, dataNotice.getContentHtml(), data);
            // 发送通知
            send(NoticeTypeEnum.SYSTEM, new DataNotifyDto()
                    .setTenantId(tenantId)
                    .setClientId(SpringContextUtil.getApplicationContextName())
                    .setUsers(users)
                    .setTitle(title)
                    .setContent(content));
            return;
        }
        extendDtos.forEach(template -> {
            // 消息标题
            String title = NoticeVariableUtils.replacementText(template.getTitleHtml(), data);
            // 消息通知内容
            String content = NoticeVariableUtils.replacement(template.getType(), template.getContentHtml(), data);
            // 模板变量
            Map<String, Map<String, String>> templateVariable = NoticeVariableUtils.replacementTemplateVariable(dataNotice.getVariable(), data);
            // 发送通知
            send(template.getType(), new DataNotifyDto()
                    .setTenantId(tenantId)
                    .setClientId(SpringContextUtil.getApplicationContextName())
                    .setUsers(users)
                    .setTitle(title)
                    .setContent(content)
                    .setTemplateVariable(templateVariable)
                    .setTemplate(template)
                    .setNoticeDataExtend(noticeDataExtend));
        });
    }

    private void send(NoticeTypeEnum noticeType, DataNotifyDto dto) {
        SendNotifyHandler sendNotifyHandler = Optional.ofNullable(flowHandlerMap.get(noticeType.getValue())).orElseThrow(() -> new BusinessException("不支持的处理类型"));
        try {
            sendNotifyHandler.send(dto);
        } catch (Exception e) {
            log.error("发送[{}]消息失败, exception: ", noticeType.getDesc(), e);
        }
    }

}
