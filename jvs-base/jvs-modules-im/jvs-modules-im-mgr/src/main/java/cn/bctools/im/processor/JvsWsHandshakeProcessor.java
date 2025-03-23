package cn.bctools.im.processor;

import cn.bctools.im.command.LoginReqHandler;
import org.jim.core.ImChannelContext;
import org.jim.core.ImPacket;
import org.jim.core.exception.ImException;
import org.jim.core.http.HttpRequest;
import org.jim.core.packets.Command;
import org.jim.core.packets.LoginReqBody;
import org.jim.core.utils.JsonKit;
import org.jim.server.JimServerAPI;
import org.jim.server.command.CommandManager;
import org.jim.server.processor.handshake.WsHandshakeProcessor;

import java.nio.charset.StandardCharsets;

/**
 * @author ZhuXiaoKang
 * @Description 自定义im socket握手处理器
 */
public class JvsWsHandshakeProcessor extends WsHandshakeProcessor {

    @Override
    public void onAfterHandshake(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
        LoginReqHandler loginHandler = (LoginReqHandler) CommandManager.getCommand(Command.COMMAND_LOGIN_REQ);
        HttpRequest request = (HttpRequest) packet;
        //UPMS登录后返回一个固定的IM登录code,通过im code转换获取用户登录对象
        String logType = request.getParams().get("logType") == null ? null : (String) request.getParams().get("logType")[0];
        String value = request.getParams().get("value") == null ? null : (String) request.getParams().get("value")[0];
        String tempLinkId = request.getParams().get("tempLinkId") == null ? null : (String) request.getParams().get("tempLinkId")[0];
        LoginReqBody loginBody = new LoginReqBody();
        loginBody.setLogType(logType);
        loginBody.setValue(value);
        loginBody.setTempLinkId(tempLinkId);
        loginBody.setCmd(Command.COMMAND_LOGIN_REQ.getNumber());
        byte[] loginBytes = JsonKit.toJsonBytes(loginBody);
        request.setBody(loginBytes);
        try {
            request.setBodyString(new String(loginBytes, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new ImException(e);
        }
        ImPacket loginRespPacket = loginHandler.handler(request, imChannelContext);
        if (loginRespPacket != null) {
            JimServerAPI.send(imChannelContext, loginRespPacket);
        }
    }
}
