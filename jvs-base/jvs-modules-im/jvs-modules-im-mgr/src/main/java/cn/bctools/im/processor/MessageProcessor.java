package cn.bctools.im.processor;

import org.jim.core.ImChannelContext;
import org.jim.core.message.MessageHelper;
import org.jim.core.packets.ChatBody;
import org.jim.core.packets.Message;
import org.jim.server.config.ImServerConfig;
import org.jim.server.processor.chat.BaseAsyncChatMessageProcessor;

/**
 * @author ZhuXiaoKang
 * @Description 持久化消息
 */
public class MessageProcessor extends BaseAsyncChatMessageProcessor {

    @Override
    protected void doProcess(ChatBody chatBody, ImChannelContext imChannelContext) {
        // 暂无实现
    }

    @Override
    public void process(ImChannelContext imChannelContext, Message message) {
        //开启持久化
        boolean isStore = ImServerConfig.ON.equals(imServerConfig.getIsStore());
        if(isStore){
            MessageHelper messageHelper = imServerConfig.getMessageHelper();
            messageHelper.writeMessage(message, imChannelContext);
        }
    }
}
