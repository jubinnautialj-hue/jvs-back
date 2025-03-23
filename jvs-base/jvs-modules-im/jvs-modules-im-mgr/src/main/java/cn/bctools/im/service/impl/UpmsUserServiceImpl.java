package cn.bctools.im.service.impl;

import cn.bctools.auth.api.dto.UserGroupDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.im.feign.api.AuthUserServiceFeignClient;
import cn.bctools.im.feign.api.UserAuthFeignClient;
import cn.bctools.im.service.UpmsUserService;
import cn.bctools.im.utils.ImLoginUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jim.core.packets.User;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: UPMS用户服务
 */

@Service
@Slf4j
@AllArgsConstructor
public class UpmsUserServiceImpl implements UpmsUserService {

    private static final int SUCCESS_CODE = 0;

    private final AuthUserServiceFeignClient authUserServiceApi;
    private final UserAuthFeignClient userAuthFeignClient;

    @Override
    public UserDto login(String loginType, String code) {
        try {
            ImLoginUtil.set(code, loginType);
            R<UserDto> result = userAuthFeignClient.userInfo();
            log.debug("token:{},获取用户信息成功:{}", code);
            return result.getData();
        } catch (Exception e) {
            log.error("IM登录失败. exception: {}", e);
        }
        return null;
    }

    @Override
    public List<UserDto> selectUsers(String tenantId) {
        TenantContextHolder.setTenantId(tenantId);
        List<UserDto> users = null;
        try {
            R<List<UserDto>> sysUsersResult = authUserServiceApi.users();
            users = sysUsersResult.getData();
            if (SUCCESS_CODE != sysUsersResult.getCode() || CollectionUtils.isEmpty(users)) {
                log.info("获取用户好友失败， tenantId：{}", tenantId);
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("获取用户好友失败. exception: {}", e);
        }
        return users;
    }

    @Override
    public List<UserGroupDto> selectGroups(String userId) {
        List<UserGroupDto> groups = null;
        try {
            R<List<UserGroupDto>> groupResult = authUserServiceApi.userGroup(userId);
            groups = groupResult.getData();
            if (SUCCESS_CODE != groupResult.getCode() || CollectionUtils.isEmpty(groups)) {
                log.info("获取用户所在用户组失败， userId：{}", userId);
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("获取用户所在用户组失败. exception: {}", e);
        }
        return groups;
    }

    @Override
    public List<User> selectGroupUserByGroupId(String groupId) {
        List<UserDto> userDtos = null;
        try {
            R<List<UserDto>> userResult = authUserServiceApi.userGroupsUser(groupId);
            userDtos = userResult.getData();
            if (SUCCESS_CODE != userResult.getCode() || CollectionUtils.isEmpty(userDtos)) {
                log.info("获取组用户信息失败， groupId：{}", groupId);
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("获取组用户信息失败. exception: {}", e);
        }

        if (CollectionUtils.isEmpty(userDtos)) {
            return Collections.emptyList();
        }
        List<User> users = new ArrayList<>();
        userDtos.stream().forEach(userDto -> users.add(convertUser(userDto)));

        return users;
    }

    /**
     * UserDto转换为User
     *
     * @param user
     * @return
     */
    private User convertUser(UserDto user) {
        return User.newBuilder()
                .userId(user.getId())
                .nick(user.getRealName())
                .avatar(user.getHeadImg())
                .realName(user.getRealName())
                .build();
    }


    @Override
    public List<UserGroupDto> userGroups(String tenantId) {
        TenantContextHolder.setTenantId(tenantId);
        List<UserGroupDto> groups = null;
        try {
            R<List<UserGroupDto>> groupResult = authUserServiceApi.userGroups();
            groups = groupResult.getData();
            if (SUCCESS_CODE != groupResult.getCode() || CollectionUtils.isEmpty(groups)) {
                log.info("获取租户所有组失败， tenantId：{}", tenantId);
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("获取租户所有组失败. exception: {}", e);
        }
        return groups;
    }
}
