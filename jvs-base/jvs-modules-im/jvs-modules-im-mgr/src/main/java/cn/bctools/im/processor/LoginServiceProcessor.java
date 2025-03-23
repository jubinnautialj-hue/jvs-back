package cn.bctools.im.processor;

import cn.bctools.auth.api.dto.UserGroupDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.im.service.UpmsUserService;
import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.jim.core.ImChannelContext;
import org.jim.core.packets.Group;
import org.jim.core.packets.User;
import org.jim.core.packets.UserStatusType;
import org.jim.core.packets.login.LoginReqBody;
import org.jim.server.protocol.AbstractProtocolCmdProcessor;
import org.jim.server.util.ChannelMarkUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author WChao
 *
 */

@Slf4j
public class LoginServiceProcessor extends AbstractProtocolCmdProcessor implements LoginCmdProcessor {

    private UpmsUserService userService = SpringContextUtil.getBean(UpmsUserService.class);

    /**
     * 根据code获取用户信息
     * @param loginReqBody
     * @param imChannelContext
     * @return
     */
    @Override
    public User getUser(LoginReqBody loginReqBody, ImChannelContext imChannelContext) {
        // 后台登录
        UserDto userDto = login(loginReqBody);
        if (userDto == null) {
            return null;
        }
        // 封装用户信息
        User user = User.newBuilder().build();
        user.setUserId(userDto.getId());
        user.setRealName(userDto.getRealName());
        user.setNick(userDto.getRealName());
        user.setAvatar(userDto.getHeadImg());
        user.setStatus(UserStatusType.ONLINE.getStatus());
        user.setTenantId(userDto.getTenantId());

        // 用户关系
        setUserRelations(user);

        return  user;
    }

    private UserDto login(LoginReqBody loginReqBody) {
        return userService.login(loginReqBody.getLogType(), loginReqBody.getValue());
    }

    private void setUserRelations(User user) {
        TenantContextHolder.setTenantId(user.getTenantId());
        //分组
        List<UserGroupDto> data = userService.selectGroups(user.getUserId());

        if (CollUtil.isNotEmpty(data)) {
            List<Group> collect = data.stream()
                    .map(e -> {
                        Group build = Group.newBuilder().groupId(e.getId()).name(e.getName()).build();
                        List<User> transition = userService.selectGroupUserByGroupId(e.getId());
                        build.setUsers(transition);
                        build.setMark(ChannelMarkUtil.buildMark(user.getTenantId(), build.getGroupId()));
                        return build;
                    }).collect(Collectors.toList());
            user.setGroups(collect);
        }
    }

    @Override
    public void onSuccess(User user, ImChannelContext channelContext) {
        log.debug("登录成功");
    }

    @Override
    public void onFailed(ImChannelContext imChannelContext) {
        log.error("登录失败");
    }

}
