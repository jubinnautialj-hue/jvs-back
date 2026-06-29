package org.jim.server.listener;

import com.alibaba.fastjson.JSONObject;
import org.jim.core.ImChannelContext;
import org.jim.core.ImConst;
import org.jim.core.cache.redis.RedisCacheManager;
import org.jim.core.exception.ImException;
import org.jim.core.listener.ImGroupListener;
import org.jim.core.message.MessageHelper;
import org.jim.core.packets.Group;
import org.jim.core.packets.User;
import org.jim.server.config.ImServerConfig;
import org.jim.server.util.MarkUtils;
import org.jim.server.util.RedisKeyUtils;

import java.util.Objects;

/**
 * @author WChao
 * @Desc

 */
public abstract class AbstractImGroupListener implements ImGroupListener {

    public abstract void doAfterBind(ImChannelContext imChannelContext, Group group) throws ImException;

    public abstract void doAfterUnbind(ImChannelContext imChannelContext, Group group) throws ImException;


    @Override
    public void onAfterBind(ImChannelContext imChannelContext, Group group) throws ImException {
        group.setGroupId(MarkUtils.parseMark(group.getGroupId()));
        Group groupCache = RedisCacheManager.getCache(ImConst.PREFIX_GROUP).get(RedisKeyUtils.GroupCache.groupInfoKey(false, imChannelContext.getTenantId(), group.getGroupId()), Group.class);
        group.setGroupType(groupCache.getGroupType());
        ImServerConfig imServerConfig = (ImServerConfig) imChannelContext.getImConfig();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        //是否开启持久化
        if (isStore(imServerConfig)) {
            messageHelper.getBindListener().onAfterGroupBind(imChannelContext, group);
        }
        doAfterBind(imChannelContext, group);
    }

    @Override
    public void onAfterUnbind(ImChannelContext imChannelContext, Group group) throws ImException {
        group.setGroupId(MarkUtils.parseMark(group.getGroupId()));
        User clientUser = imChannelContext.getSessionContext().getImClientNode().getUser();
        imChannelContext.setUserId(clientUser.getUserId());
        Group groupCache = RedisCacheManager.getCache(ImConst.PREFIX_GROUP).get(RedisKeyUtils.GroupCache.groupInfoKey(false, clientUser.getTenantId(), group.getGroupId()), Group.class);
        group.setGroupType(groupCache.getGroupType());
        ImServerConfig imServerConfig = (ImServerConfig) imChannelContext.getImConfig();
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        //是否开启持久化
        if (isStore(imServerConfig)) {
            messageHelper.getBindListener().onAfterGroupUnbind(imChannelContext, groupCache);
        }
        doAfterUnbind(imChannelContext, group);
    }

    /**
     * 是否开启持久化;
     *
     * @return
     */
    public boolean isStore(ImServerConfig imServerConfig) {
        MessageHelper messageHelper = imServerConfig.getMessageHelper();
        return ImServerConfig.ON.equals(imServerConfig.getIsStore()) && Objects.nonNull(messageHelper);
    }

}
