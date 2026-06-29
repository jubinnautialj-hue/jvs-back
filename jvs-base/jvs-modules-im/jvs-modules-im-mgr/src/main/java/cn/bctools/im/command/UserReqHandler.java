package cn.bctools.im.command;

import cn.bctools.im.utils.ImLoginUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jim.core.ImChannelContext;
import org.jim.core.ImPacket;
import org.jim.core.ImSessionContext;
import org.jim.core.ImStatus;
import org.jim.core.exception.ImException;
import org.jim.core.packets.Command;
import org.jim.core.packets.RespBody;
import org.jim.core.packets.UserReqBody;
import org.jim.core.packets.UserStatusType;
import org.jim.core.utils.JsonKit;
import org.jim.server.command.AbstractCmdHandler;
import org.jim.server.command.handler.userInfo.IUserInfo;
import org.jim.server.command.handler.userInfo.PersistentUserInfo;
import org.jim.server.protocol.ProtocolManager;

import java.util.Objects;

/**
 * 获取用户信息请求cmd消息命令处理器
 */
@Slf4j
public class UserReqHandler extends AbstractCmdHandler {

    // 模拟查询用户信息
    private IUserInfo persistentUserInfo;

    public UserReqHandler() {
        this.persistentUserInfo = new PersistentUserInfo();
    }

    @Override
    public Command command() {
        return Command.COMMAND_GET_USER_REQ;
    }

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        UserReqBody userReqBody = JsonKit.toBean(packet.getBody(), UserReqBody.class);
        log.debug("接收到获取用户信息请求：{}", JSON.toJSONString(userReqBody));
        String userId = userReqBody.getUserId();
        if(StringUtils.isEmpty(userId)) {
            return ProtocolManager.Converter.respPacket(new RespBody(Command.COMMAND_GET_USER_RESP, ImStatus.C10004), imChannelContext);
        }
        // 0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线]
        Integer type = userReqBody.getType() == null ? UserStatusType.ALL.getNumber() : userReqBody.getType();
        if(Objects.isNull(UserStatusType.valueOf(type))){
            return ProtocolManager.Converter.respPacket(new RespBody(Command.COMMAND_GET_USER_RESP, ImStatus.C10004), imChannelContext);
        }

        RespBody resPacket = new RespBody(Command.COMMAND_GET_USER_RESP);
        ImSessionContext imSessionContext = imChannelContext.getSessionContext();
        ImLoginUtil.set(imSessionContext.getToken(), imSessionContext.getVersion());

        // 获取用户信息
        resPacket.setData(getUserInfo(userReqBody, imChannelContext));

        // 在线用户
        if(UserStatusType.ONLINE.getNumber() == userReqBody.getType()){
            resPacket.setCode(ImStatus.C10000.getCode()).setMsg(ImStatus.C10005.getMsg());
            // 离线用户
        }else if(UserStatusType.OFFLINE.getNumber() == userReqBody.getType()){
            resPacket.setCode(ImStatus.C10000.getCode()).setMsg(ImStatus.C10006.getMsg());
            // 在线+离线用户
        }else if(UserStatusType.ALL.getNumber() == userReqBody.getType()){
            resPacket.setCode(ImStatus.C10000.getCode()).setMsg(ImStatus.C10003.getMsg());
        }
        ImLoginUtil.clear();
        return ProtocolManager.Converter.respPacket(resPacket, imChannelContext);
    }

    /**
     * 调用获取用户信息接口
     * @param userReqBody
     * @return
     */
    private Object getUserInfo(UserReqBody userReqBody, ImChannelContext imChannelContext) {
        return persistentUserInfo.getUserInfo(userReqBody, imChannelContext);
    }

}
