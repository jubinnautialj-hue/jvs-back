package org.jim.server.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jim.core.ImChannelContext;
import org.jim.core.ImConst;
import org.jim.core.ImSessionContext;
import org.jim.core.config.ImConfig;
import org.jim.core.packets.User;
import org.jim.core.packets.notify.NotifyReqBody;
import org.jim.core.session.id.impl.UUIDSessionIdGenerator;
import org.jim.core.utils.JsonKit;
import org.jim.server.JimServerAPI;
import org.jim.server.config.ImServerConfig;

import java.util.List;

/**
 * IM通知工具类
 */
public class NotifyKit {

    private static Logger log = Logger.getLogger(NotifyKit.class);

    /**
     * 转换为聊天消息结构;
     * @param body
     * @param imChannelContext
     * @return
     */
    public static NotifyReqBody toNotifyBody(byte[] body, ImChannelContext imChannelContext){
        NotifyReqBody notifyReqBody = parseNotifyBody(body);
        if(notifyReqBody != null){
            if(StringUtils.isEmpty(notifyReqBody.getFrom())){
                ImSessionContext imSessionContext = imChannelContext.getSessionContext();
                User user = imSessionContext.getImClientNode().getUser();
                if(user != null){
                    notifyReqBody.setFrom(user.getUserId());
                }else{
                    notifyReqBody.setFrom(imChannelContext.getId());
                }
            }
        }
        return notifyReqBody;
    }

    /**
     * 判断是否属于指定格式聊天消息;
     * @param body
     * @return
     */
    private static NotifyReqBody parseNotifyBody(byte[] body){
        if(body == null) {
            return null;
        }
        NotifyReqBody notifyReqBody = null;
        try{
            String text = new String(body, ImConst.CHARSET);
            notifyReqBody = JsonKit.toBean(text,NotifyReqBody.class);
            if(notifyReqBody != null){
                if(notifyReqBody.getCreateTime() == null) {
                    notifyReqBody.setCreateTime(System.currentTimeMillis());
                }
                if(StringUtils.isEmpty(notifyReqBody.getId())){
                    notifyReqBody.setId(UUIDSessionIdGenerator.instance.sessionId(null));
                }
                return notifyReqBody;
            }
        }catch(Exception e){
            log.error(e.toString());
        }
        return notifyReqBody;
    }

    /**
     * 判断用户是否在线
     * @param tenantId 租户id
     * @param userId 用户ID
     * @param isStore 是否开启持久化(true:开启,false:未开启)
     * @return
     */
    public static boolean isOnline(String tenantId, String userId , boolean isStore){
        if(isStore){
            ImServerConfig imServerConfig = ImConfig.Global.get();
            return imServerConfig.getMessageHelper().isOnline(tenantId, userId);
        }
        List<ImChannelContext> imChannelContexts = JimServerAPI.getByUserId(userId);
        if(CollectionUtils.isNotEmpty(imChannelContexts)){
            return true;
        }
        return false;
    }
}
