package cn.bctools.message.push.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.message.push.dto.enums.MessageTypeEnum;
import cn.bctools.message.push.dto.enums.PlatformEnum;
import cn.bctools.message.push.dto.messagepush.EmailMessageDto;
import cn.bctools.message.push.handler.EmailMessageHandler;
import cn.bctools.message.push.service.EmailMessagePushService;
import cn.bctools.message.push.utils.MessagePushHisUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author xh
 */
@Slf4j
@Service
@AllArgsConstructor
public class EmailMessagePushServiceImpl implements EmailMessagePushService {

    private final EmailMessageHandler emailMessageHandler;
    private final MessagePushHisUtils messagePushHisUtils;

    @Override
    public void sendEmailMessage(EmailMessageDto messageDto) {
        try {
            //设置租户id
            String batchNumber = messagePushHisUtils.saveHis(messageDto, PlatformEnum.EMAIL, MessageTypeEnum.EMAIL);
            emailMessageHandler.handle(batchNumber);
        } catch (Exception e) {
            log.error(" send error", e);
        }
    }

    @Override
    public void resendEmailMessage(String pushHisId) {
        try {
            emailMessageHandler.resend(pushHisId);
        } catch (Exception e) {
            log.error(" send error", e);
        }
    }
}
