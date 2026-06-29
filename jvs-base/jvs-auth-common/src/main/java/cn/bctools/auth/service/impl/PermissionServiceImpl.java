package cn.bctools.auth.service.impl;

import cn.bctools.auth.constants.AuthConstant;
import cn.bctools.common.utils.jvs.JvsSystemConfig;
import cn.bctools.auth.entity.RolePermission;
import cn.bctools.auth.mapper.RolePermissionMapper;
import cn.bctools.auth.service.PermissionService;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.gateway.entity.Permission;
import cn.bctools.gateway.entity.TypeEnum;
import cn.bctools.gateway.mapper.PermissionMapper;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author
 */
@Slf4j
@Service
@AllArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    RolePermissionMapper rolePermissionMapper;
    DiscoveryClient discoveryClient;
    JvsSystemConfig jvsSystemConfig;

    /**
     * 根据用户ID查询所有的资源信息
     *
     * @param rolesIds 角色ID
     * @return 权限资源信息
     */
    public List<Permission> queryUserPermission(List<String> rolesIds) {
        List<RolePermission> permissionList = rolePermissionMapper.selectList(Wrappers.<RolePermission>lambdaQuery()
                .in(RolePermission::getRoleId, rolesIds));
        List<String> ids = permissionList
                .stream()
                .map(RolePermission::getPermissionId)
                .filter(ObjectUtil::isNotNull)
                .distinct()
                .collect(Collectors.toList());
        // 移除非纯数字的资源id（保存角色-资源关系时，可能会将资源所属组名作为id存储）
        ids.removeIf(id -> !id.matches("\\d+"));
        if (ObjectUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return this.list(Wrappers.<Permission>lambdaQuery()
                .select(Permission::getPermission, Permission::getClientName)
                .in(Permission::getId, ids));
    }

    @Override
    public List<String> getPermission(Boolean platformAdmin, Boolean adminFlag, String tenantId, List<String> roleIds) {
        List<Permission> permissionList = new ArrayList<>();
        List<Permission> permissionListAll = list();
        //判断是否最上级租户的管理员,如果是,则返回所有资源
        if (platformAdmin) {
            // 如果是平台管理员则返回所有资源
            permissionList = permissionListAll;
        } else if (adminFlag) {
            permissionList = permissionListAll.stream().filter(e -> ObjectNull.isNotNull(e.getType())).filter(e -> e.getType().equals(TypeEnum.tenant)).collect(Collectors.toList());
        } else if (ObjectNull.isNotNull(roleIds)) {
            permissionList = queryUserPermission(roleIds);
        }
        // 如果是非平台管理员 的其它管理员, 返回除运维管理的资源
        // 如果是管理员, 返回所有资源.
        // 如果是超级管理员，直接查询租户角色权限

        //目前只获取后台有权限其它没有
        List<String> permission = new ArrayList<>();
        permissionList.stream()
                .map(Permission::getPermission)
                .filter(ObjectNull::isNotNull)
                .forEach(permission::add);
        if (!jvsSystemConfig.getMultiTenantMode()) {
            //如果是单租户模式,直接删除多租户资源
            permission.remove("jvs_tenant");
        }
        if (ObjectNull.isNotNull(jvsSystemConfig.getService())) {
            //添加动态资源服务
            permission.addAll(jvsSystemConfig.getService().stream().map(e -> e.getName().name()).collect(Collectors.toList()));
        }
        if (ObjectNull.isNotNull(jvsSystemConfig.getIdentifications())) {
            //额外的标识信息是否启动
            permission.addAll(jvsSystemConfig.getIdentifications());
        }
        if (permissionList.stream().noneMatch(e -> AuthConstant.jvs_base.equals(e.getClientName()))) {
            //添加后台
            permission.remove(AuthConstant.jvs_base_permission);
        } else {
            permission.add(AuthConstant.jvs_base_permission);
        }
        //判断是否有平台资源如果有，授权平台资源
        if (permissionList.stream().anyMatch(e -> AuthConstant.jvs_platform.equals(e.getClientName()))) {
            //添加后台
            permission.add(AuthConstant.jvs_platform_permission);
        } else {
            permission.remove(AuthConstant.jvs_platform_permission);
        }
        //如果有 ai ，都有这个标识
        if (ObjectNull.isNotNull(jvsSystemConfig.getAidomain())) {
            //所有人都有这个权限
            permission.add(AuthConstant.jvs_ai);
        } else {
            permission.remove(AuthConstant.jvs_ai);
        }
        return permission;
    }
}
