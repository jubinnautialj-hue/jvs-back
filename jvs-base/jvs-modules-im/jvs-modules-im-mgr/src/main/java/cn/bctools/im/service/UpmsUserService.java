package cn.bctools.im.service;

import cn.bctools.auth.api.dto.UserGroupDto;
import cn.bctools.common.entity.dto.UserDto;
import org.jim.core.packets.User;

import java.util.List;

/**
 * @author ZhuXiaoKang
 * @Description UPMS用户服务
 */
public interface UpmsUserService {

    /**
     * 登录
     * @param loginType 路由版本号
     * @param code 用户token
     * @return
     */
    UserDto login(String loginType, String code);

    /**
     * 获取登录用户的好友,默认为当前登录用户所属组织的所有人
     * @param tenantId 当前登录用户租户id
     * @return
     */
    List<UserDto> selectUsers(String tenantId);

    /**
     * 查询当前用户所在所有部门以及各部门下所有用户
     * @param userId 当前登录用户id
     * @return
     */
    List<UserGroupDto> selectGroups(String userId);

    /**
     * 查询指定组所有用户
     * @param groupId 组id
     * @return
     */
    List<User> selectGroupUserByGroupId(String groupId);

    /**
     * 查询租户下所有组
     * @param tenantId
     * @return
     */
    List<UserGroupDto> userGroups(String tenantId);
}
