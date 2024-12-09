package cn.bctools.auth.service;

import cn.bctools.auth.entity.Role;
import cn.bctools.auth.entity.User;
import cn.bctools.auth.entity.UserRole;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 用户角色
 *
 * @author
 */
public interface UserRoleService extends IService<UserRole> {

    /**
     * 根据用户id查询角色信息集合
     *
     * @param userId 用户ID集
     * @return 角色信息集合
     */
    List<Role> getRoleByUserId(String userId);

    /**
     * 根据用户id数组查询角色信息
     *
     * @param userId 用户id数组
     * @return 以用户id为key的map数据
     */
    Map<String, List<Role>> getRoleByUserId(Collection<String> userId);

    /**
     * 根据角色id查询用户信息集合
     *
     * @param roleId 角色id
     * @return 用户信息集合
     */
    List<User> getUsersByRoleId(String roleId);

    /**
     * 赋予指定用户默认的系统角色
     *
     * @param userId 用户id
     */
    void grandDefaultSysRole(String userId);

    /**
     * 移除指定用户信息
     *
     * @param userId 用户id
     */
    void clearUser(@NotNull String userId);

    /**
     * 移除指定角色所有用户
     *
     * @param roleId
     */
    void clearRoleUser(@NotNull String roleId);

}
