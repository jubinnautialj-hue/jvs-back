package cn.bctools.im.client.handler;

import lombok.extern.slf4j.Slf4j;
import org.jim.core.ImChannelContext;
import org.jim.core.ImConst;
import org.jim.core.ImPacket;
import org.jim.core.config.ImConfig;
import org.jim.core.exception.ImDecodeException;
import org.jim.core.exception.ImException;
import org.jim.core.packets.Command;
import org.jim.core.tcp.TcpPacket;
import org.jim.core.tcp.TcpServerDecoder;
import org.jim.core.tcp.TcpServerEncoder;

import java.nio.ByteBuffer;

/**
 * @ClassName DefaultImClientHandler
 * @Description 默认的IM客户端回调
 * @Author WChao
 * @Version 1.0
 **/
@Slf4j
public class DefaultImClientHandler implements ImClientHandler {
    private static TcpPacket heartbeatPacket = new TcpPacket(Command.COMMAND_HEARTBEAT_REQ,new byte[]{ImConst.Protocol.HEARTBEAT_BYTE});


    @Override
    public void handler(ImPacket imPacket, ImChannelContext imChannelContext) throws ImException {

    }

    @Override
    public ByteBuffer encode(ImPacket imPacket, ImConfig imConfig, ImChannelContext imChannelContext) {
        TcpPacket tcpPacket = (TcpPacket)imPacket;
        return TcpServerEncoder.encode(tcpPacket, imConfig, imChannelContext);
    }

    @Override
    public ImPacket decode(ByteBuffer buffer, int limit, int position, int readableLength, ImChannelContext imChannelContext) throws ImDecodeException {
        TcpPacket tcpPacket = TcpServerDecoder.decode(buffer, imChannelContext);
        return tcpPacket;
    }

    @Override
    public ImPacket heartbeatPacket(ImChannelContext imChannelContext) {
        return heartbeatPacket;
    }

}
