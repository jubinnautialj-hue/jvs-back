package cn.bctools.im.command;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.im.utils.ChannelTenantUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jim.core.ImChannelContext;
import org.jim.core.ImPacket;
import org.jim.core.ImSessionContext;
import org.jim.core.exception.ImException;
import org.jim.core.packets.*;
import org.jim.core.utils.JsonKit;
import org.jim.server.ImServerChannelContext;
import org.jim.server.JimServerAPI;
import org.jim.server.command.AbstractCmdHandler;
import org.jim.server.command.CommandManager;
import org.jim.server.command.handler.JoinGroupReqHandler;
import org.jim.server.protocol.ProtocolManager;
import org.jim.server.queue.MsgQueueRunnable;
import org.jim.server.util.BusinessKit;
import org.jim.server.util.ChannelMarkUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @Author: ZhuXiaoKang
 * @Description: 文档消息请求cmd消息命令处理器
 */
@Slf4j
public class BusinessReqHandler extends AbstractCmdHandler {

    @Override
    public Command command() {
        return Command.COMMAND_BUSINESS_REQ;
    }

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext channelContext) throws ImException {
        ImServerChannelContext imServerChannelContext = (ImServerChannelContext)channelContext;
        if (packet.getBody() == null) {
            throw new ImException("body is null");
        }
        BusinessBody businessBody =  BusinessKit.toBusinessBody(packet.getBody(), channelContext);
        businessBody.setTenantId(ChannelTenantUtil.build(channelContext.getTenantId(), businessBody.getTenantId()));
        log.debug("接收到文档消息请求：{}", JSON.toJSONString(businessBody));
        if (StringUtils.isBlank(businessBody.getTenantId())) {
            return ProtocolManager.Packet.dataInCorrect(Command.COMMAND_BUSINESS_RESP, channelContext);
        }

        // 数据校验
        if (Boolean.FALSE.equals(checkBody(businessBody))) {
            return ProtocolManager.Packet.dataInCorrect(Command.COMMAND_BUSINESS_RESP, channelContext);
        }

        packet.setBody(businessBody.toByte());

        // 一个自定义业务id相当于一个群（如：一个文档，可以由多人编辑，那么这个文档的id，就相当于群组id）
        String mark = ChannelMarkUtil.buildMark(businessBody.getTenantId(), businessBody.getBusinessId());

        //异步调用业务处理消息接口
        MsgQueueRunnable msgQueueRunnable = getMsgQueueRunnable(imServerChannelContext);
        msgQueueRunnable.addMsg(businessBody);
        msgQueueRunnable.executor.execute(msgQueueRunnable);

        // 推送
        if (businessBody.getContent() != null) {
            ImPacket businessPacket = new ImPacket(Command.COMMAND_BUSINESS_RESP, new RespBody(Command.COMMAND_BUSINESS_RESP, businessBody).toByte());
            JimServerAPI.sendToGroup(mark, businessPacket);
        }

        return ProtocolManager.Packet.success(Command.COMMAND_BUSINESS_RESP, channelContext);
    }


    /**
     * 通知参数校验
     * @param businessBody 业务消息体
     * @return false：校验失败， true，校验通过
     */
    private Boolean checkBody(BusinessBody businessBody) {
        if (businessBody == null) {
            return Boolean.FALSE;
        }
        if (StringUtils.isBlank(businessBody.getBusinessId())) {
            return Boolean.FALSE;
        }
        if (businessBody.getExpire() == null) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 获取通知业务处理异步消息队列
     * @param imServerChannelContext IM通道上下文
     * @return
     */
    private MsgQueueRunnable getMsgQueueRunnable(ImServerChannelContext imServerChannelContext){
        MsgQueueRunnable msgQueueRunnable = (MsgQueueRunnable)imServerChannelContext.getMsgQue();
        if(ObjectNull.isNotNull(msgQueueRunnable.getProtocolCmdProcessor())){
            return msgQueueRunnable;
        }
        synchronized (MsgQueueRunnable.class){
            msgQueueRunnable.setProtocolCmdProcessor(this.getSingleProcessor());
        }
        return msgQueueRunnable;
    }
}
