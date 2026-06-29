package cn.bctools.message.push.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.message.push.dto.enums.MessageTypeEnum;
import cn.bctools.message.push.dto.enums.PlatformEnum;
import cn.bctools.message.push.dto.messagepush.InsideNotificationDto;
import cn.bctools.message.push.handler.InsideNotificationHandler;
import cn.bctools.message.push.service.InsideNotificationService;
import cn.bctools.message.push.utils.MessagePushHisUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author czy
 */
@Slf4j
@Service
@AllArgsConstructor
public class InsideNotificationServiceImpl implements InsideNotificationService {

    private final InsideNotificationHandler insideNotificationHandler;
    private final MessagePushHisUtils messagePushHisUtils;

    @Override
    public void send(InsideNotificationDto dto) {
        try {
            //设置租户id
            String batchNumber = messagePushHisUtils.saveHis(dto, PlatformEnum.INSIDE_NOTIFICATION, MessageTypeEnum.INSIDE_NOTIFICATION);
            insideNotificationHandler.handle(batchNumber);
        } catch (Exception e) {
            log.error(" send error", e);

        }
    }

    @Override
    public void resend(String pushHisId) {
        try {
            insideNotificationHandler.resend(pushHisId);
        } catch (Exception e) {
            log.error(" send error", e);

        }
    }
}
