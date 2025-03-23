package org.jim.server.processor;

import org.jim.core.ImChannelContext;
import org.jim.core.ImConst;
import org.jim.core.packets.Message;

/**
 * @author WChao
 * @Desc 不同协议CMD命令处理器接口
*/
public interface ProtocolCmdProcessor extends ImConst {
    /**
     * cmd命令处理器方法
     * @param imChannelContext IM通道上下文
     * @param message 消息
     */
    void process(ImChannelContext imChannelContext, Message message);

}
