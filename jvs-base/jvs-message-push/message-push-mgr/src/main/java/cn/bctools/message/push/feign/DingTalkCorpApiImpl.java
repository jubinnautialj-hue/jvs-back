package cn.bctools.message.push.feign;

import cn.bctools.common.utils.R;
import cn.bctools.message.push.api.DingTalkCorpApi;
import cn.bctools.message.push.dto.messagepush.AliSmsDto;
import cn.bctools.message.push.dto.messagepush.dingtalk.corp.*;
import cn.bctools.message.push.rabbitmq.Producer;
import cn.bctools.message.push.service.DingTalkCorpService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xh
 */
@RequestMapping
@RestController
@AllArgsConstructor
public class DingTalkCorpApiImpl implements DingTalkCorpApi {
    DingTalkCorpService dingTalkCorpService;
    private final Producer<TextMessageDTO> producer;

    @Override
    public R<Boolean> sendActionCardMultiMessage(ActionCardMultiMessageDTO dto) {
        return null;
    }

    @Override
    public R<Boolean> resendActionCardMultiMessage(String pushHisId) {
        return null;
    }

    @Override
    public R<Boolean> sendActionCardSingleMessage(ActionCardSingleMessageDTO dto) {
        dingTalkCorpService.sendActionCardSingleMessage(dto);
        return R.ok(true);
    }

    @Override
    public R<Boolean> resendActionCardSingleMessage(String pushHisId) {
        return null;
    }

    @Override
    public R<Boolean> sendLinkMessage(LinkMessageDTO dto) {
        dingTalkCorpService.sendLinkMessage(dto);
        return R.ok(true);
    }

    @Override
    public R<Boolean> resendLinkMessage(String pushHisId) {
        return null;
    }

    @Override
    public R<Boolean> sendMarkdownMessage(MarkdownMessageDTO dto) {
        return null;
    }

    @Override
    public R<Boolean> resendMarkdownMessage(String pushHisId) {
        return null;
    }

    @Override
    public R<Boolean> sendOaMessage(OaMessageDTO dto) {
        return null;
    }

    @Override
    public R<Boolean> resendOaMessage(String pushHisId) {
        return null;
    }

    @Override
    public R<Boolean> sendTextMessage(TextMessageDTO dto) {
        dingTalkCorpService.sendTextMessage(dto);
        return R.ok(true);
    }

    @Override
    public R<Boolean> sendAsyncTextMessage(TextMessageDTO dto) {
        producer.push2Mq(dto);
        return R.ok(true);
    }

    @Override
    public R<Boolean> resendTextMessage(String pushHisId) {
        return null;
    }
}
