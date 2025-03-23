package cn.bctools.im.service;

import org.jim.core.packets.User;

import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: 社交关系服务
 */
public interface SocialService {

    /**
     * 获取用户的所有好友不包括自己
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @return
     */
    List<User> getFriends(String tenantId, String userId);
}
