package cn.bctools.im.command;

import cn.bctools.im.processor.LoginCmdProcessor;
import cn.bctools.im.utils.ImLoginUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jim.core.ImChannelContext;
import org.jim.core.ImPacket;
import org.jim.core.ImSessionContext;
import org.jim.core.config.ImConfig;
import org.jim.core.exception.ImException;
import org.jim.core.message.MessageHelper;
import org.jim.core.packets.Command;
import org.jim.core.packets.Group;
import org.jim.core.packets.User;
import org.jim.core.packets.login.LoginReqBody;
import org.jim.core.packets.login.LoginRespBody;
import org.jim.core.protocol.IProtocol;
import org.jim.core.utils.JsonKit;
import org.jim.server.ImServerChannelContext;
import org.jim.server.JimServerAPI;
import org.jim.server.command.AbstractCmdHandler;
import org.jim.server.command.CommandManager;
import org.jim.server.command.handler.JoinGroupReqHandler;
import org.jim.server.config.ImServerConfig;
import org.jim.server.protocol.ProtocolManager;
import org.jim.server.util.ChannelMarkUtil;

import java.util.List;
import java.util.Objects;

/**
 * 登录请求cmd消息命令处理器
 */
@Slf4j
public class LoginReqHandler extends AbstractCmdHandler {

    @Override
    public Command command() {
        return Command.COMMAND_LOGIN_REQ;
    }

    @Override
    public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        LoginReqBody loginReqBody = JsonKit.toBean(packet.getBody(), LoginReqBody.class);
        log.debug("接收到登录请求：{}", JSON.toJSONString(loginReqBody));
        LoginCmdProcessor loginProcessor = this.getSingleProcessor(LoginCmdProcessor.class);
        LoginRespBody loginRespBody = LoginRespBody.success();
        User user = getUserByProcessor(imChannelContext, loginProcessor, loginReqBody, loginRespBody);
        loginRespBody.setData(user);
        ImSessionContext imSessionContext = imChannelContext.getSessionContext();
        imSessionContext.setTempLinkId(loginReqBody.getTempLinkId());
        imSessionContext.setToken(loginReqBody.getValue());
        imSessionContext.setVersion(loginReqBody.getLogType());
        return ProtocolManager.Converter.respPacket(loginRespBody, imChannelContext);
    }

    /**
     * 根据用户配置的自定义登录处理器获取业务组装的User信息
     * @param imChannelContext 通道上下文
     * @param loginProcessor 登录自定义业务处理器
     * @param loginReqBody 登录请求体
     * @param loginRespBody 登录响应体
     * @return 用户组装的User信息
     * @throws ImException
     */
    private User getUserByProcessor(ImChannelContext imChannelContext, LoginCmdProcessor loginProcessor, LoginReqBody loginReqBody, LoginRespBody loginRespBody)throws ImException{
        // 获取用户信息
        User user = loginProcessor.getUser(loginReqBody, imChannelContext);

        if (user == null) {
            loginRespBody = LoginRespBody.failed();
            log.error("login failed, logType:{}, value:{}", loginReqBody.getLogType(), loginReqBody.getValue());
            loginProcessor.onFailed(imChannelContext);
            JimServerAPI.bSend(imChannelContext, ProtocolManager.Converter.respPacket(loginRespBody, imChannelContext));
            JimServerAPI.remove(imChannelContext, "value is incorrect");
            return null;
        }

        ImServerChannelContext imServerChannelContext = (ImServerChannelContext)imChannelContext;
        IProtocol protocol = imServerChannelContext.getProtocolHandler().getProtocol();
        user.setTerminal(Objects.isNull(protocol) ? Protocol.UNKNOWN : protocol.name());
        user.setMark(ChannelMarkUtil.buildMark(user.getTenantId(), user.getUserId()));

        JimServerAPI.bindUser(imServerChannelContext, user);

        // 初始化绑定或者解绑群组
        initGroup(imChannelContext, user);

        // 清空组信息
        user.setGroups(null);

        loginProcessor.onSuccess(user, imChannelContext);

        ImLoginUtil.clear();
        return user;
    }

    /**
     * 初始化绑定或者解绑群组;
     */
    public void initGroup(ImChannelContext imChannelContext , User user)throws ImException{
        String userId = user.getUserId();
        List<Group> groups = user.getGroups();
        if(CollectionUtils.isEmpty(groups)) {
            return;
        }
        ImServerConfig imServerConfig = ImConfig.Global.get();
        boolean isStore = ImServerConfig.ON.equals(imServerConfig.getIsStore());
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        // 解绑未关联的组（场景：登录后，redis中保存了关联的群组，在下一次登录的之前，关联的群组有变更，解绑不再关联的群组）
        List<String> groupIds = null;
        if(isStore){
            groupIds = messageHelper.getGroups(user.getTenantId(), userId);
        }
        //绑定群组
        for(Group group : groups){
            if(isStore && CollectionUtils.isNotEmpty(groupIds)){
                groupIds.remove(group.getGroupId());
            }
            ImPacket groupPacket = new ImPacket(Command.COMMAND_JOIN_GROUP_REQ,JsonKit.toJsonBytes(group));
            try {
                JoinGroupReqHandler joinGroupReqHandler = CommandManager.getCommand(Command.COMMAND_JOIN_GROUP_REQ, JoinGroupReqHandler.class);
                joinGroupReqHandler.handler(groupPacket, imChannelContext);
            } catch (Exception e) {
                log.error(e.toString(),e);
            }
        }
        if(isStore && groupIds != null){
            for(String groupId : groupIds){
                messageHelper.getBindListener().onAfterGroupUnbind(imChannelContext, Group.newBuilder().groupId(groupId).build());
            }
        }
    }
}
