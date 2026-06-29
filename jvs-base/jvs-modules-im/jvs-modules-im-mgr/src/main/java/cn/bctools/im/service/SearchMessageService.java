package cn.bctools.im.service;

import org.jim.core.message.MessageHelper;
import org.jim.core.packets.UserMessageData;
import org.jim.core.packets.message.MessageReqBody;

/**
 * @author ZhuXiaoKang
 * @Description 消息查询服务
 */
public interface SearchMessageService {

    /**
     * 类型
     * @return
     */
    String getType();

    /**
     * 查询消息
     * @param messageReqBody
     * @param messageHelper
     * @return
     */
    UserMessageData search(MessageReqBody messageReqBody, MessageHelper messageHelper);
}
