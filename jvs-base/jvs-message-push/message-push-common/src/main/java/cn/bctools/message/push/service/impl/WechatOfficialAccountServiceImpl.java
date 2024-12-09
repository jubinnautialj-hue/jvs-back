package cn.bctools.message.push.service.impl;

import cn.bctools.message.push.dto.enums.MessageTypeEnum;
import cn.bctools.message.push.dto.enums.PlatformEnum;
import cn.bctools.message.push.dto.messagepush.wechatofficialaccount.TemplateMessageDTO;
import cn.bctools.message.push.dto.vo.WxMpTemplateVo;
import cn.bctools.message.push.handler.wechatofficialaccount.MpNewsMessageHandler;
import cn.bctools.message.push.handler.wechatofficialaccount.MpTextMessageHandler;
import cn.bctools.message.push.handler.wechatofficialaccount.TemplateMessageHandler;
import cn.bctools.message.push.service.WechatOfficialAccountService;
import cn.bctools.message.push.utils.MessagePushHisUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xh
 */
@Slf4j
@Service
@AllArgsConstructor
public class WechatOfficialAccountServiceImpl implements WechatOfficialAccountService {

    private final MpNewsMessageHandler newsMessageHandler;
    private final MpTextMessageHandler textMessageHandler;
    private final TemplateMessageHandler templateMessageHandler;
    private final MessagePushHisUtils messagePushHisUtils;

    @Override
    public void sendWebChatTemplateMessage(TemplateMessageDTO messageDto) {
        try {
            //设置租户id
            String batchNumber = messagePushHisUtils.saveHis(messageDto, PlatformEnum.WECHAT_OFFICIAL_ACCOUNT, MessageTypeEnum.WECHAT_OFFICIAL_ACCOUNT_TEMPLATE);
            templateMessageHandler.handle(batchNumber);
        } catch (Exception e) {
            log.error("消息发送异常", e);
        }
    }

    @Override
    public void resendWebChatTemplateMessage(String pushHisId) {
        try {
            templateMessageHandler.resend(pushHisId);
        } catch (Exception e) {
            log.error("send error", e);
        }
    }

    @Override
    public List<WxMpTemplateVo> getAllPrivateTemplate() {
        return templateMessageHandler.getAllTemplate();
    }
}
