package cn.bctools.message.push.service;


import cn.bctools.message.push.dto.messagepush.wechatwork.robot.ImageMessageDTO;
import cn.bctools.message.push.dto.messagepush.wechatwork.robot.MarkdownMessageDTO;
import cn.bctools.message.push.dto.messagepush.wechatwork.robot.NewsMessageDTO;
import cn.bctools.message.push.dto.messagepush.wechatwork.robot.TextMessageDTO;

/**
 * @author wl
 */
public interface WechatWorkRobotService {
    /**
     * 发送消息
     *
     * @param messageDto
     */
    void sendWebChatImageMessage(ImageMessageDTO messageDto);

    /**
     * 发送消息
     *
     * @param messageDto
     */
    void sendWebChatMarkDownMessage(MarkdownMessageDTO messageDto);

    /**
     * 发送消息
     *
     * @param messageDto
     */
    void sendWebChatNewsMessage(NewsMessageDTO messageDto);

    /**
     * 发送消息
     *
     * @param messageDto
     */
    void sendWebChatTextMessage(TextMessageDTO messageDto);

}
