package cn.bctools.message.push.service;

import cn.bctools.message.push.dto.messagepush.wechatwork.agent.*;

/**
 * @author xh
 */
public interface WechatWorkAgentService {
    /**
     * 发送文字消息
     *
     * @param messageDto
     */
    void sendWebChatWorkFileMessage(MediaMessageDTO messageDto);

    /**
     * 发送图文消息
     *
     * @param messageDto
     */
    void sendWebChatImageMessage(MediaMessageDTO messageDto);

    /**
     * 企业微信Markdown
     *
     * @param messageDto
     */
    void sendWebChatMarkDownMessage(MarkdownMessageDTO messageDto);

    /**
     * 企业微信图文消息
     *
     * @param messageDto
     */
    void sendWebChatNewsMessage(NewsMessageDTO messageDto);

    /**
     * 发送卡片消息
     *
     * @param messageDto
     */
    void sendWebChatTextCardMessage(TextCardMessageDTO messageDto);

    /**
     * 企业微信消息
     *
     * @param messageDto
     */
    void sendWebChatTextMessage(WeTextMessageDTO messageDto);

    /**
     * 发送视频消息
     *
     * @param messageDto
     */
    void sendWebChatVideoMessage(VideoMessageDTO messageDto);
}
