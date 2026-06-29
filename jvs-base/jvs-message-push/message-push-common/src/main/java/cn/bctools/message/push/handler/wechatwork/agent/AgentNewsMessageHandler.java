package cn.bctools.message.push.handler.wechatwork.agent;

import cn.bctools.auth.api.api.AuthTenantConfigServiceApi;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigEnterriseWeChat;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.message.push.dto.enums.MessagePushStatusEnum;
import cn.bctools.message.push.dto.enums.MessageTypeEnum;
import cn.bctools.message.push.dto.enums.PlatformEnum;
import cn.bctools.message.push.dto.messagepush.wechatwork.agent.NewsMessageDTO;
import cn.bctools.message.push.entity.MessagePushHis;
import cn.bctools.message.push.handler.BaseMessageHandler;
import cn.bctools.message.push.service.MessagePushHisService;
import cn.bctools.message.push.utils.SingletonUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import me.chanjar.weixin.cp.api.impl.WxCpServiceImpl;
import me.chanjar.weixin.cp.bean.article.NewArticle;
import me.chanjar.weixin.cp.bean.message.WxCpMessage;
import me.chanjar.weixin.cp.config.impl.WxCpDefaultConfigImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 企业微信图文消息handler
 *
 * @author wl
 */
@Component
@AllArgsConstructor
public class AgentNewsMessageHandler extends BaseMessageHandler<NewsMessageDTO> {
    private final MessagePushHisService messagePushHisService;
    private final AuthTenantConfigServiceApi configServiceApi;

    @Override
    public void handle(NewsMessageDTO param) {
        if (!param.hasReceiver()) {
            throw new BusinessException("没有检测到接收人配置");
        }
        //获取配置详情
        String body = configServiceApi.key(ConfigsTypeEnum.ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION, TenantContextHolder.getTenantId()).getData();
        SysConfigEnterriseWeChat config = configServiceApi.convertKey(ConfigsTypeEnum.ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION, body);

        if (!config.getEnable()) {
            throw new IllegalArgumentException("未找到邮件配置，请核实!");
        }

        WxCpServiceImpl wxCpService = SingletonUtil.get(config.getAppId() + config.getAppSecret() + config.getAgentId(), () -> {
            WxCpDefaultConfigImpl cpConfig = new WxCpDefaultConfigImpl();
            cpConfig.setBaseApiUrl(config.getBaseApiUrl());
            cpConfig.setCorpId(config.getAppId());
            cpConfig.setCorpSecret(config.getAppSecret());
            cpConfig.setAgentId(config.getAgentId());
            WxCpServiceImpl wxCpService1 = new WxCpServiceImpl();
            wxCpService1.setWxCpConfigStorage(cpConfig);
            return wxCpService1;
        });

        //生成批次号
        String batchNumber = UUID.randomUUID().toString().replaceAll("-", "");

        param.getDefinedReceivers().forEach(e -> {
            MessagePushHis messagePushHis = new MessagePushHis()
                    .setMessageContent(JSON.toJSONString(param))
                    .setBatchNumber(batchNumber)
                    .setUserId(e.getUserId())
                    .setUserName(e.getUserName())
                    .setClientCode(param.getClientCode())
                    .setPlatform(PlatformEnum.WECHAT_WORK_AGENT)
                    .setMessageType(MessageTypeEnum.WECHAT_WORK_AGENT_NEWS);

            List<NewArticle> articles =
                    param.getArticles()
                            .stream()
                            .map(s -> BeanCopyUtil.copy(s,NewArticle.class))
                            .collect(Collectors.toList());

            WxCpMessage message = WxCpMessage.NEWS()
                    .agentId(config.getAgentId()) // 企业号应用ID
                    .toUser(e.getReceiverConfig())
                    .toParty(param.getToParty())
                    .toTag(param.getToTag())
                    .articles(articles)
                    .build();
            try {
                wxCpService.getMessageService().send(message);
                messagePushHis.setPushStatus(MessagePushStatusEnum.SUCCESS);
            } catch (Exception exception) {
                messagePushHis.setPushStatus(MessagePushStatusEnum.FAILED);
                String eMessage = ExceptionUtil.getMessage(exception);
                eMessage = StringUtils.isBlank(eMessage) ? "未知错误" : eMessage;
                messagePushHis.setErrorMsg(eMessage);
            }
            messagePushHisService.save(messagePushHis);
        });
    }

    @Override
    public void resend(String pushHisId) throws Exception {
        MessagePushHis his = messagePushHisService.getById(pushHisId);
        NewsMessageDTO dto = JSON.parseObject(his.getMessageContent(), NewsMessageDTO.class);
        dto.getDefinedReceivers().removeIf(e -> e.getUserId() == null || !e.getUserId().equals(his.getUserId()));
        handle(dto);
    }
}
