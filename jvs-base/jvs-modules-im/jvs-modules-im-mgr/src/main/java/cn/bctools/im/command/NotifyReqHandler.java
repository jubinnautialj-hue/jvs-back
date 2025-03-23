package cn.bctools.im.command;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.im.service.UpmsUserService;
import cn.bctools.im.utils.ChannelTenantUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jim.core.ImChannelContext;
import org.jim.core.ImPacket;
import org.jim.core.config.ImConfig;
import org.jim.core.exception.ImException;
import org.jim.core.packets.Command;
import org.jim.core.packets.RespBody;
import org.jim.core.packets.notify.NotifyReqBody;
import org.jim.core.packets.notify.NotifyTypeEnum;
import org.jim.server.ImServerChannelContext;
import org.jim.server.JimServerAPI;
import org.jim.server.command.AbstractCmdHandler;
import org.jim.server.config.ImServerConfig;
import org.jim.server.protocol.ProtocolManager;
import org.jim.server.queue.MsgQueueRunnable;
import org.jim.server.util.ChannelMarkUtil;
import org.jim.server.util.NotifyKit;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 消息推送请求cmd消息命令处理器
 */
@Slf4j
public class NotifyReqHandler extends AbstractCmdHandler {

    private UpmsUserService upmsUserService = SpringContextUtil.getBean(UpmsUserService.class);

    @Override
    public Command command() {
        return Command.COMMAND_NOTIFY_REQ;
    }

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext channelContext) throws ImException {
        ImServerChannelContext imServerChannelContext = (ImServerChannelContext)channelContext;
        if (packet.getBody() == null) {
            throw new ImException("body is null");
        }
        NotifyReqBody notifyBody = NotifyKit.toNotifyBody(packet.getBody(), channelContext);
        notifyBody.setTenantId(ChannelTenantUtil.build(channelContext.getTenantId(), notifyBody.getTenantId()));
        log.debug("接收到推送通知请求：{}", JSON.toJSONString(notifyBody));
        if (StringUtils.isBlank(notifyBody.getTenantId())) {
            return ProtocolManager.Packet.dataInCorrect(Command.COMMAND_NOTIFY_RESP, channelContext);
        }

        // 数据校验
        if (Boolean.FALSE.equals(checkBody(notifyBody))) {
            return ProtocolManager.Packet.dataInCorrect(Command.COMMAND_NOTIFY_RESP, channelContext);
        }
        packet.setBody(notifyBody.toByte());

        //异步调用业务处理消息接口
        MsgQueueRunnable msgQueueRunnable = getMsgQueueRunnable(imServerChannelContext);
        msgQueueRunnable.addMsg(notifyBody);
        msgQueueRunnable.executor.execute(msgQueueRunnable);

        ImPacket notifyPacket = new ImPacket(Command.COMMAND_NOTIFY_RESP, new RespBody(Command.COMMAND_NOTIFY_RESP, notifyBody).toByte());
        // 设置同步序列号
        notifyPacket.setSynSeq(packet.getSynSeq());

        return sendMessage(notifyBody, notifyPacket, channelContext);

    }


    /**
     * 通知参数校验
     * @param notifyBody 通知消息体
     * @return false：校验失败， true，校验通过
     */
    private Boolean checkBody(NotifyReqBody notifyBody) {
        if (notifyBody == null) {
            return Boolean.FALSE;
        }
        if (NotifyTypeEnum.forNumber(notifyBody.getNotifyType()) == null) {
            return Boolean.FALSE;
        }
        // 广播，tenantId不能为空
        if (NotifyTypeEnum.NOTIFY_TYPE_BROADCAST.getNumber() == notifyBody.getNotifyType() && CollectionUtils.isEmpty(notifyBody.getTenantIds())) {
            return Boolean.FALSE;
        }
        // 组推送，toGroupIds不能为空
        if (NotifyTypeEnum.NOTIFY_TYPE_GROUP.getNumber() == notifyBody.getNotifyType() && CollectionUtils.isEmpty(notifyBody.getToGroupIds())) {
            return Boolean.FALSE;
        }
        // 精确推送，toUserIds不能为空
        if (NotifyTypeEnum.NOTIFY_TYPE_ACCURATE.getNumber() == notifyBody.getNotifyType() && CollectionUtils.isEmpty(notifyBody.getToUserIds())) {
            return Boolean.FALSE;
        }
        if (StringUtils.isEmpty(notifyBody.getTitle())) {
            return Boolean.FALSE;
        }
        if (StringUtils.isEmpty(notifyBody.getContent())) {
            return Boolean.FALSE;
        }
        return true;
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

    /**
     * 发送消息
     * @param notifyBody
     * @return
     */
    private ImPacket sendMessage(NotifyReqBody notifyBody, ImPacket notifyPacket, ImChannelContext channelContext)  throws ImException {
        ImServerConfig imServerConfig = ImConfig.Global.get();
        boolean isStore = ImServerConfig.ON.equals(imServerConfig.getIsStore());
        // 广播
        if (NotifyTypeEnum.NOTIFY_TYPE_BROADCAST.getNumber() == notifyBody.getNotifyType()) {
            notifyBroadcast(notifyBody, isStore, notifyPacket);
        }

        // 组推送
        if (NotifyTypeEnum.NOTIFY_TYPE_GROUP.getNumber() == notifyBody.getNotifyType()) {
            notifyGroup(notifyBody, notifyPacket);
        }

        // 精确推送
        if (NotifyTypeEnum.NOTIFY_TYPE_ACCURATE.getNumber() == notifyBody.getNotifyType()) {
            notifyAccurate(notifyBody, isStore, notifyPacket);
        }
        return ProtocolManager.Packet.success(Command.COMMAND_NOTIFY_RESP, channelContext);
    }

    /**
     * 广播
     * @param notifyBody
     * @param isStore
     * @param notifyPacket
     */
    private void notifyBroadcast(NotifyReqBody notifyBody, boolean isStore, ImPacket notifyPacket) {
        for (String toTenantId : notifyBody.getTenantIds()) {

            List<UserDto> imUsers = upmsUserService.selectUsers(toTenantId);
            if (CollectionUtils.isEmpty(imUsers)) {
                continue;
            }
            for (UserDto imUser : imUsers) {
                if (NotifyKit.isOnline(toTenantId, imUser.getId(), isStore)) {
                    JimServerAPI.sendToUser(ChannelMarkUtil.buildMark(toTenantId, imUser.getId()), notifyPacket);
                }
            }

        }
    }


    /**
     * 精确推送
     * @param notifyBody
     * @param isStore
     * @param notifyPacket
     */
    private void notifyAccurate(NotifyReqBody notifyBody, boolean isStore, ImPacket notifyPacket) {
        for (String toUserId : notifyBody.getToUserIds()) {
            if (NotifyKit.isOnline(notifyBody.getTenantId(), toUserId, isStore)) {
                JimServerAPI.sendToUser(ChannelMarkUtil.buildMark(notifyBody.getTenantId(), toUserId), notifyPacket);
            }
        }
    }

    /**
     * 组推送
     * @param notifyBody
     * @param notifyPacket
     */
    private void notifyGroup(NotifyReqBody notifyBody, ImPacket notifyPacket) {
        for (String toGroupId : notifyBody.getToGroupIds()) {
            // 按组推送
            JimServerAPI.sendToGroup(ChannelMarkUtil.buildMark(notifyBody.getTenantId(), toGroupId), notifyPacket);
        }
    }


}
