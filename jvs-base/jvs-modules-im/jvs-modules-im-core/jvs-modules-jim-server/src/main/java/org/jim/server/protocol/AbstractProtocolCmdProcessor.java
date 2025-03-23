package org.jim.server.protocol;

import org.jim.core.ImChannelContext;
import org.jim.core.packets.Message;
import org.jim.server.processor.ProtocolCmdProcessor;

/**
 * @author WChao
 * @Desc
*/
public abstract class AbstractProtocolCmdProcessor implements ProtocolCmdProcessor {

    @Override
    public void process(ImChannelContext imChannelContext, Message message) {

    }
}
