package cn.bctools.im.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.im.service.SocialService;
import cn.bctools.im.service.UpmsUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jim.core.packets.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: ZhuXiaoKang
 * @Description: 社交关系服务——Jvs实现
 */
@ConditionalOnProperty(name = "social", havingValue = "userAuth", matchIfMissing = true)
@Service
@Slf4j
@AllArgsConstructor
public class SocialJvsServiceImpl implements SocialService {

    private final UpmsUserService upmsUserService;

    @Override
    public List<User> getFriends(String tenantId, String userId) {
        List<UserDto> userDtos = upmsUserService.selectUsers(tenantId);
        if (CollectionUtils.isEmpty(userDtos)) {
            return Collections.emptyList();
        }

        List<User> users = userDtos.stream().filter(e -> !userId.equals(e.getId()))
                .map(e -> convertUser(e)).collect(Collectors.toList());
        return users;
    }

    /**
     * UserDto转换为User
     *
     * @param userDto
     * @return
     */
    private User convertUser(UserDto userDto) {
        return User.newBuilder()
                .userId(userDto.getId())
                .nick(userDto.getRealName())
                .avatar(userDto.getHeadImg())
                .realName(userDto.getRealName())
                .build();
    }
}
