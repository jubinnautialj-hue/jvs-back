package org.jim.server.command.handler.userInfo;

import org.jim.core.ImChannelContext;
import org.jim.core.config.ImConfig;
import org.jim.core.message.MessageHelper;
import org.jim.core.packets.User;
import org.jim.core.packets.UserReqBody;
import org.jim.server.config.ImServerConfig;

import java.util.Objects;

/**
 * 持久化获取用户信息处理
 */
public class PersistentUserInfo implements IUserInfo {

    @Override
    public User getUserInfo(UserReqBody userReqBody, ImChannelContext imChannelContext) {
        ImServerConfig imServerConfig = ImConfig.Global.get();
        String userId = userReqBody.getUserId();
        Integer type = userReqBody.getType();
        String tenantId = imChannelContext.getTenantId();
        //消息持久化助手;
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        User user = messageHelper.getUserByType(tenantId, userId, type);
        if(Objects.nonNull(user)) {
            user.setFriends(messageHelper.getAllFriendUsers(tenantId, userId, type));
            user.setGroups(messageHelper.getAllGroupUsers(tenantId, userId, type, imChannelContext));
        }
        return user;
    }

}
