package cn.bctools.auth.service;

import cn.bctools.gateway.entity.Permission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * The interface Permission service.
 * 权限的系统资源标识接口服务 ，资源标识分为平台级和租户级
 *
 * @author jvs
 */
public interface PermissionService extends IService<Permission> {

    /**
     * 处理缓存,如果角色有变动自动清楚缓存
     * 根据租户,角色,是否是管理员,返回相关标识
     * 后续添加终端权限,区分不同的服务避免前端返回数据太多
     *
     * @param platformAdmin 是否是平台管理员
     * @param adminFlag     是否是租户管理员
     * @param tenantId      租户id
     * @param roleIds       角色ids
     * @return permission
     */
    List<String> getPermission(Boolean platformAdmin, Boolean adminFlag, String tenantId, List<String> roleIds);
}
