package cn.bctools.auth.service;

import cn.bctools.auth.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 角色服务
 *
 * @author
 */
public interface RoleService extends IService<Role> {

    /**
     * 获取成员范围是"所有人"的角色id集合
     *
     * @return 角色id集合
     */
    List<String> getScopeAllRoles();

}
