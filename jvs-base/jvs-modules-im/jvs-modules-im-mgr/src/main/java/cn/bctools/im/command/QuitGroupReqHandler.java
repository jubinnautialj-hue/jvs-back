package cn.bctools.im.command;

import cn.bctools.im.utils.ChannelTenantUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jim.core.ImChannelContext;
import org.jim.core.ImPacket;
import org.jim.core.ImSessionContext;
import org.jim.core.exception.ImException;
import org.jim.core.packets.Command;
import org.jim.core.packets.Group;
import org.jim.core.packets.User;
import org.jim.core.utils.JsonKit;
import org.jim.server.JimServerAPI;
import org.jim.server.command.AbstractCmdHandler;
import org.jim.server.protocol.ProtocolManager;
import org.jim.server.util.ChannelMarkUtil;

/**
 * @Author: ZhuXiaoKang
 * @Description: 退出群组
 */
@Slf4j
public class QuitGroupReqHandler extends AbstractCmdHandler {

    @Override
    public Command command() {
        return Command.COMMAND_QUIT_GROUP_REQ;
    }

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext channelContext) throws ImException {
        Group group = JsonKit.toBean(packet.getBody(), Group.class);
        ImSessionContext imSessionContext = channelContext.getSessionContext();
        User user = imSessionContext.getImClientNode().getUser();
        group.setTenantId(ChannelTenantUtil.build(channelContext.getTenantId(), user.getTenantId()));
        log.debug("接收到退出群组请求：{}", JSON.toJSONString(group));
        if (StringUtils.isBlank(group.getGroupId())) {
            return ProtocolManager.Packet.dataInCorrect(Command.COMMAND_QUIT_GROUP_RESP, channelContext);
        }
        String mark = ChannelMarkUtil.buildMark(group.getTenantId(), group.getGroupId());
        JimServerAPI.unbindGroup(mark, channelContext);
        return null;
    }
}
