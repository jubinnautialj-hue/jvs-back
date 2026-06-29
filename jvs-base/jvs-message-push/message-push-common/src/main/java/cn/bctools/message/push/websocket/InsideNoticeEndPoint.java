package cn.bctools.message.push.websocket;

import cn.bctools.message.push.entity.InsideNotice;
import cn.bctools.message.push.service.impl.InsideNoticeServiceImpl;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author czy
 */
@Slf4j
@Component
@ServerEndpoint(value = "/inside/notice/{userId}/{tenantId}")
public class InsideNoticeEndPoint {
    public static final String SOCKET_KEY = "%s_%s";

    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, InsideNoticeEndPoint>> WEB_SOCKET_CONTAINER = new ConcurrentHashMap<>();

    private String userId;

    private String sessionId;

    private Session session;

    private String tenantId;

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId, @PathParam("tenantId") String tenantId) {
        this.sessionId = session.getId();
        this.userId = userId;
        this.session = session;
        this.tenantId = tenantId;
        String format = String.format(SOCKET_KEY, userId, tenantId);
        if (WEB_SOCKET_CONTAINER.containsKey(format)) {
            WEB_SOCKET_CONTAINER.get(format).put(session.getId(), this);
        } else {
            ConcurrentHashMap<String, InsideNoticeEndPoint> websocketMap = new ConcurrentHashMap<>(1);
            websocketMap.put(session.getId(), this);
            WEB_SOCKET_CONTAINER.put(format, websocketMap);
        }
        this.sendMessageTo(format);
        log.info(userId + ":" + this.sessionId + "已连接");
    }

    @OnClose
    public void onClose(Session session) {
        String userId = this.userId;
        String sessionId = this.sessionId;
        String tenantId = this.tenantId;
        String socketId = String.format(SOCKET_KEY, userId, tenantId);
        WEB_SOCKET_CONTAINER.get(socketId).remove(sessionId);
        if (WEB_SOCKET_CONTAINER.get(socketId).isEmpty()) {
            WEB_SOCKET_CONTAINER.remove(socketId);
        }
        log.info(userId + ":" + this.sessionId + "已断开连接");
    }

    @OnMessage
    public void onMessage(String message,Session session){
        session.getAsyncRemote().sendText("{}");
    }

    /**
     * 错误时调用
     *
     * @param session
     * @param throwable
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("站内信异常",throwable);
    }

    public void sendMessageTo(InsideNotice insideNotice) {
        String userId = insideNotice.getUserId();
        String tenantId = insideNotice.getTenantId();
        InsideNoticeServiceImpl insideNoticeService = this.getInsideNoticeService();
        long count = insideNoticeService.count(new LambdaQueryWrapper<InsideNotice>().eq(InsideNotice::getUserId, userId)
                .eq(InsideNotice::getReadIs, Boolean.FALSE));
        Map<String, Object> msgMap = new HashMap<>(1);
        msgMap.put("remainingCount", count);

        String socketKey = String.format(SOCKET_KEY, userId, tenantId);
        if (WEB_SOCKET_CONTAINER.containsKey(socketKey)) {
            if (!userId.equals(insideNotice.getCreateById())) {
                ConcurrentHashMap<String, InsideNoticeEndPoint> websocketMap = WEB_SOCKET_CONTAINER.get(socketKey);
                websocketMap.forEach((e, webSocket) -> webSocket.session.getAsyncRemote().sendText(JSONUtil.toJsonStr(msgMap)));
            }

            if (userId.equals(insideNotice.getCreateById())) {
                //修改阅读条件
                insideNotice.setReadIs(Boolean.TRUE);
                //修改阅读状态
                insideNoticeService.updateById(insideNotice);
            }
        }
    }

    public void sendMessageTo(String socketKey) {
        InsideNoticeServiceImpl insideNoticeService = this.getInsideNoticeService();
        long count = insideNoticeService.count(new LambdaQueryWrapper<InsideNotice>().eq(InsideNotice::getUserId, this.userId)
                .eq(InsideNotice::getTenantId, this.tenantId)
                .eq(InsideNotice::getReadIs, Boolean.FALSE));
        Map<String, Object> msgMap = new HashMap<>(1);
        msgMap.put("remainingCount", count);
        if (WEB_SOCKET_CONTAINER.containsKey(socketKey)) {
            ConcurrentHashMap<String, InsideNoticeEndPoint> websocketMap = WEB_SOCKET_CONTAINER.get(socketKey);
            websocketMap.forEach(
                    (e, webSocket) -> {
                        if (this.sessionId.equals(webSocket.sessionId)) {
                            webSocket.session.getAsyncRemote().sendObject(JSONUtil.toJsonStr(msgMap));
                        }
                    });
        }
    }

    private InsideNoticeServiceImpl getInsideNoticeService() {
        return SpringUtil.getBean("insideNoticeServiceImpl");
    }
}
