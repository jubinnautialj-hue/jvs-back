package cn.bctools.message.push.service;

import cn.bctools.message.push.dto.messagepush.dingtalk.corp.*;

/**
 * @author xh
 */
public interface DingTalkCorpService {

    /**
     * 发送消息
     *
     * @param dto
     */
    void sendActionCardMultiMessage(ActionCardMultiMessageDTO dto);

    /**
     * 发送消息
     *
     * @param dto
     */
    void sendActionCardSingleMessage(ActionCardSingleMessageDTO dto);

    /**
     * 发送消息
     *
     * @param dto
     */
    void sendLinkMessage(LinkMessageDTO dto);

    /**
     * 发送消息
     *
     * @param dto
     */
    void sendMarkdownMessage(MarkdownMessageDTO dto);

    /**
     * 发送消息
     *
     * @param dto
     */
    void sendOaMessage(OaMessageDTO dto);

    /**
     * 发送消息
     *
     * @param dto
     */
    void sendTextMessage(TextMessageDTO dto);
}
