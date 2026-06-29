package cn.bctools.im.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.im.constants.ArangoConstants;
import cn.bctools.im.entity.arangodb.ImFriend;
import cn.bctools.im.entity.arangodb.ImUser;
import cn.bctools.im.service.ImFriendService;
import cn.bctools.im.service.ImUserService;
import cn.bctools.im.service.SocialService;
import cn.bctools.im.service.UpmsUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jim.core.packets.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: 社交关系服务——Arangodb实现
 */
@ConditionalOnProperty(name = "social", havingValue = "arangodb", matchIfMissing = true)
@Service
@Slf4j
@AllArgsConstructor
public class SocialArangoServiceImpl implements SocialService {

    private final UpmsUserService upmsUserService;

    private final ImUserService imUserService;

    private final ImFriendService imFriendService;


    @Override
    public List<User> getFriends(String tenantId, String userId) {
        List<User> friends = new ArrayList<>();

        // 从Arangodb获取好友关系
        List<ImFriend> imFriends = imFriendService.getUserFriends(tenantId, userId);
        if (CollectionUtils.isNotEmpty(imFriends)) {
            imFriends.forEach(imFriend ->
                    friends.add(convertUser(imFriend.getTo()))
            );
        }

        // 若没有好友，则构造好友（因为默认用户的好友为当前登录用户所属组织的所有人）
        if (CollectionUtils.isEmpty(friends)) {
            return buildFriends(tenantId, userId);
        }

        return friends;
    }

    /**
     * 构造好友（因为默认用户的好友为当前登录用户所属组织的所有人）
     * - 若组织用户未同步，则同步组织用户
     * - 同步完成后，创建当前用户与本组织其它用户的好友关系
     *
     * @param tenantId
     * @param userId
     */
    private List<User> buildFriends(String tenantId, String userId) {
        List<User> friends = new ArrayList<>();

        // 从Arangodb查询组织所有用户信息
        List<ImUser> imUsers = imUserService.getAllByTenantId(tenantId);
        if (CollectionUtils.isEmpty(imUsers)) {
            log.info("租户{}的用户数据未同步至ArangoDB,开始同步", tenantId);
            List<UserDto> userDtos = upmsUserService.selectUsers(tenantId);
            if (CollectionUtils.isEmpty(userDtos)) {
                return Collections.emptyList();
            }
            imUsers = new ArrayList<>();
            // 将用户信息批量保存到Arangodb
            for (UserDto userDto : userDtos) {
                String id = userDto.getId();
                ImUser imUser = new ImUser();
                imUser.setKey(ArangoConstants.buildUserKey(tenantId, id));
                imUser.setRealName(userDto.getRealName());
                imUser.setUserId(id);
                imUser.setAvatar(userDto.getHeadImg());
                imUser.setNick(userDto.getRealName());
                imUser.setTenantId(tenantId);
                imUsers.add(imUser);
            }
            imUserService.saveAll(imUsers);
            log.info("租户{}的用户数据未同步ArangoDB,同步完成", tenantId);
        }

        // 创建当前用户与所属租户所有用户的好友关系
        log.info("创建用户{}与租户{}所有用户的好友关系", userId, tenantId);
        ImUser form = new ImUser();
        form.setKey(ArangoConstants.buildUserKey(tenantId, userId));
        List<ImFriend> imFriends = new ArrayList<>();
        imUsers.forEach(imUser -> {
            if (!imUser.getUserId().equals(userId)) {
                // 封装im好友信息
                friends.add(convertUser(imUser));

                // 封装Arangodb好友关系
                ImUser to = new ImUser();
                to.setKey(imUser.getKey());
                ImFriend imFriend = new ImFriend();
                imFriend.setFrom(form);
                imFriend.setTo(to);
                imFriends.add(imFriend);
            }
        });
        imFriendService.saveAll(imFriends);
        log.info("创建用户{}与租户{}所有用户的好友关系完成", userId, tenantId);

        return friends;
    }

    /**
     * ImUser转换为User
     *
     * @param imUser
     * @return
     */
    private User convertUser(ImUser imUser) {
        return User.newBuilder()
                .userId(imUser.getUserId())
                .nick(imUser.getNick())
                .avatar(imUser.getAvatar())
                .realName(imUser.getRealName())
                .build();
    }

}
