package cn.bctools.message.push.service;


import cn.bctools.message.push.dto.messagepush.dingtalk.robot.*;

/**
 * @author wl
 */
public interface DingTalkRobotService {

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
    void sendFeedCardMessage(FeedCardMessageDTO dto);

    /**
     * 发送消息
     *
     * @param dto
     */
    void sendTextMessage(TextMessageDTO dto);

}
