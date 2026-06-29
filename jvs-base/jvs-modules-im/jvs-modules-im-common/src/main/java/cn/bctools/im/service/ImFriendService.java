package cn.bctools.im.service;

import cn.bctools.im.entity.arangodb.ImFriend;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: IM好友服务
 */

public interface ImFriendService {

    /**
     * 查询目标用户好友
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @return
     */
    List<ImFriend> getUserFriends(String tenantId, String userId);

    /**
     * 批量创建好友关系
     * @param imFriends
     */
    void saveAll(List<ImFriend> imFriends);
}
