package cn.bctools.auth.service;

import cn.bctools.auth.entity.DeptRole;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.validation.constraints.NotNull;

/**
 * @author zhuxiaokang
 * 部门角色
 */
public interface DeptRoleService extends IService<DeptRole> {


    /**
     * 移除指定角色所有部门
     *
     * @param roleId
     */
    void clearRoleDept(@NotNull String roleId);

}
