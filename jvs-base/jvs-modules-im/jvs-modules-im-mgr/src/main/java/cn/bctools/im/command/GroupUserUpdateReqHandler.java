package cn.bctools.im.command;

import cn.bctools.im.utils.ChannelTenantUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.jim.core.ImChannelContext;
import org.jim.core.ImPacket;
import org.jim.core.ImSessionContext;
import org.jim.core.config.ImConfig;
import org.jim.core.exception.ImException;
import org.jim.core.packets.Command;
import org.jim.core.packets.RespBody;
import org.jim.core.packets.User;
import org.jim.core.packets.group.GroupUserUpdateReqBody;
import org.jim.core.utils.JsonKit;
import org.jim.server.JimServerAPI;
import org.jim.server.command.AbstractCmdHandler;
import org.jim.server.config.ImServerConfig;
import org.jim.server.protocol.ProtocolManager;
import org.jim.server.util.ChannelMarkUtil;

/**
 * @Author: ZhuXiaoKang
 * @Description: 组用户信息修改请求cmd消息命令处理器
 */
@Slf4j
public class GroupUserUpdateReqHandler extends AbstractCmdHandler {

    @Override
    public Command command() {
        return Command.COMMAND_GROUP_USER_UPDATE_REQ;
    }

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext channelContext) throws ImException {
        GroupUserUpdateReqBody groupUserUpdateReqBody = JsonKit.toBean(packet.getBody(), GroupUserUpdateReqBody.class);
        ImSessionContext imSessionContext = channelContext.getSessionContext();
        User user = imSessionContext.getImClientNode().getUser();
        groupUserUpdateReqBody.setTenantId(ChannelTenantUtil.build(channelContext.getTenantId(), user.getTenantId()));
        groupUserUpdateReqBody.setUserId(user.getUserId());
        log.debug("接收到组用户信息修改请求：{}", JSON.toJSONString(groupUserUpdateReqBody));

        String mark = ChannelMarkUtil.buildMark(groupUserUpdateReqBody.getTenantId(), groupUserUpdateReqBody.getGroupId());

        // 推送
        ImPacket updatePacket = new ImPacket(Command.COMMAND_GROUP_USER_UPDATE_RESP, new RespBody(Command.COMMAND_GROUP_USER_UPDATE_RESP, JSON.toJSONString(groupUserUpdateReqBody)).toByte());
        JimServerAPI.sendToGroup(mark, updatePacket,channelContext.getTioChannelContext().userid);

        // 修改组用户信息(当前仅支持修改扩展信息)
        ImServerConfig imServerConfig = ImConfig.Global.get();
        imServerConfig.getMessageHelper().updateGroupUser(user.getUserId(), groupUserUpdateReqBody, channelContext);

        return ProtocolManager.Packet.success(Command.COMMAND_GROUP_USER_UPDATE_RESP, channelContext);
    }
}
