package cn.bctools.im.command;

import cn.bctools.im.utils.ChannelTenantUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jim.core.ImChannelContext;
import org.jim.core.ImPacket;
import org.jim.core.ImSessionContext;
import org.jim.core.ImStatus;
import org.jim.core.config.ImConfig;
import org.jim.core.exception.ImException;
import org.jim.core.packets.Command;
import org.jim.core.packets.Group;
import org.jim.core.packets.RespBody;
import org.jim.core.packets.User;
import org.jim.core.utils.JsonKit;
import org.jim.server.command.AbstractCmdHandler;
import org.jim.server.config.ImServerConfig;
import org.jim.server.protocol.ProtocolManager;

/**
 * @Author: ZhuXiaoKang
 * @Description: 获取指定群组用户请求cmd消息命令处理器
 */
@Slf4j
public class GetGroupUserReqHandler extends AbstractCmdHandler {

    @Override
    public Command command() {
        return Command.COMMAND_GET_GROUP_USER_REQ;
    }

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext channelContext) throws ImException {
        Group group = JsonKit.toBean(packet.getBody(), Group.class);
        ImSessionContext imSessionContext = channelContext.getSessionContext();
        User user = imSessionContext.getImClientNode().getUser();
        group.setTenantId(ChannelTenantUtil.build(channelContext.getTenantId(), user.getTenantId()));
        log.debug("接收到获取指定群组用户请求请求：{}", JSON.toJSONString(group));
        if (StringUtils.isBlank(group.getGroupId())) {
            return ProtocolManager.Packet.dataInCorrect(Command.COMMAND_QUIT_GROUP_RESP, channelContext);
        }
        // 获取组用户信息
        ImServerConfig imServerConfig = ImConfig.Global.get();
        Group groupUsers = imServerConfig.getMessageHelper().getGroupUsers(user.getTenantId(), group.getGroupId(), 2, channelContext);
        RespBody resPacket = new RespBody(Command.COMMAND_GET_GROUP_USER_RESP);
        resPacket.setCode(ImStatus.C10000.getCode()).setMsg(ImStatus.C10000.getMsg());
        resPacket.setData(groupUsers);
        return ProtocolManager.Converter.respPacket(resPacket, channelContext);
    }
}
