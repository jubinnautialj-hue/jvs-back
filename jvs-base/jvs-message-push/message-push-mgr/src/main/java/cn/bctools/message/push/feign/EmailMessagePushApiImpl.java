package cn.bctools.message.push.feign;

import cn.bctools.common.utils.R;
import cn.bctools.message.push.api.EmailMessagePushApi;
import cn.bctools.message.push.dto.messagepush.EmailMessageDto;
import cn.bctools.message.push.rabbitmq.Producer;
import cn.bctools.message.push.service.EmailMessagePushService;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xh
 */
@RequestMapping
@RestController
@AllArgsConstructor
@Api(tags = "[Feign] 邮件接口")
public class EmailMessagePushApiImpl implements EmailMessagePushApi {

    private final EmailMessagePushService emailMessagePushService;
    private final Producer<EmailMessageDto> producer;
    @Override
    public R<Boolean> sendEmail(EmailMessageDto messageDto) {
        producer.push2Mq(messageDto);
        return R.ok(Boolean.TRUE);
    }

    @Override
    public R<Boolean> resendEmail(String pushHisId) {
        emailMessagePushService.resendEmailMessage(pushHisId);
        return R.ok(Boolean.TRUE);
    }
}
