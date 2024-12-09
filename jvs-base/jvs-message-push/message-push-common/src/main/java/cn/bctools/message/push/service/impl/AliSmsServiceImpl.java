package cn.bctools.message.push.service.impl;

import cn.bctools.message.push.dto.enums.MessageTypeEnum;
import cn.bctools.message.push.dto.enums.PlatformEnum;
import cn.bctools.message.push.dto.messagepush.AliSmsDto;
import cn.bctools.message.push.dto.vo.AliSmsTemplateVo;
import cn.bctools.message.push.handler.AliSmsHandler;
import cn.bctools.message.push.service.AliSmsService;
import cn.bctools.message.push.utils.MessagePushHisUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author guojing
 */
@Service
@Slf4j
@AllArgsConstructor
public class AliSmsServiceImpl implements AliSmsService {

    private final AliSmsHandler aliSmsHandler;
    private final MessagePushHisUtils messagePushHisUtils;

    @Override
    public void send(AliSmsDto dto) {
        try {
            //储存消息日志
            String batchNumber = messagePushHisUtils.saveHis(dto, PlatformEnum.ALI_SMS, MessageTypeEnum.ALI_SMS);
            aliSmsHandler.handle(batchNumber);
        } catch (Exception e) {
            log.error(" send error", e);

            //throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public void resend(String pushHisId) {
        try {
            aliSmsHandler.resend(pushHisId);
        } catch (Exception e) {
            log.error(" send error", e);

        }
    }

    @Override
    public List<AliSmsTemplateVo> getAllPrivateTemplate(Integer pageIndex, Integer pageSize) {
        return aliSmsHandler.getAllTemplate(pageIndex, pageSize);
    }
}
