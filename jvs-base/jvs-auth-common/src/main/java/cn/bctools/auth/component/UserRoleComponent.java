package cn.bctools.auth.component;

import cn.bctools.auth.entity.*;
import cn.bctools.auth.entity.enums.RoleMemberScopeEnum;
import cn.bctools.auth.service.*;
import cn.bctools.common.entity.dto.DeptDto;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TreeUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zhuxiaokang
 * 用户角色
 */
@Component
@Slf4j
@AllArgsConstructor
public class UserRoleComponent {

    private final UserService userService;
    private final DeptService deptService;
    private final RoleService roleService;
    private final UserRoleService userRoleService;
    private final UserTenantService userTenantService;
    private final DeptRoleService deptRoleService;

    /**
     * 为用户设置默认角色
     *
     * @param userId
     */
    public void grandDefaultSysRole(String userId) {
        userRoleService.grandDefaultSysRole(userId);
    }

    /**
     * 获取用户所有角色
     * 包括：用户关联的角色、成员范围为”所有人“的角色、用户所属部门关联的角色
     *
     * @param userId 用户id
     * @return 角色id集合
     */
    public List<String> getUserRoleIds(String userId) {
        List<String> roleIds = new ArrayList<>();
        // 获取用户角色
        List<String> userRoleIds = userRoleService.list(Wrappers.<UserRole>lambdaQuery()
                        .select(UserRole::getRoleId)
                        .eq(UserRole::getUserId, userId))
                .stream().map(UserRole::getRoleId).collect(Collectors.toList());
        if (ObjectNull.isNotNull(userRoleIds)) {
            roleIds.addAll(userRoleIds);
        }
        // 获取成员范围是"所有人"的角色id集合
        List<String> scopeAllRoles = roleService.getScopeAllRoles();
        if (ObjectNull.isNotNull(scopeAllRoles)) {
            roleIds.addAll(scopeAllRoles);
        }
        // 获取用户所属部门相关的角色
        List<DeptDto> userDeptId = new ArrayList<>();
        UserTenant one = userTenantService.getOne(Wrappers.<UserTenant>lambdaQuery()
                .eq(UserTenant::getUserId, userId));
        if (ObjectNull.isNotNull(one.getDeptId())) {
            userDeptId = deptService.listByIds(one.getDeptId()).stream().map(e -> new DeptDto().setDeptId(e.getId()).setDeptName(e.getDeptCode()).setDeptName(e.getName())).collect(Collectors.toList());
        }
        if (ObjectNull.isNotNull(userDeptId)) {
            List<Dept> deptList = deptService.list();
            List<DeptRole> deptRoles = deptRoleService.list();
            List<String> list = userDeptId.stream().map(DeptDto::getDeptId).collect(Collectors.toList());

            List<String> deptRoleIds = deptRoles.stream()
                    .filter(role -> {
                        // 不包括部门的下级部门，判断部门id是否与当前用户部门相同
                        if (Boolean.FALSE.equals(role.getBelow())) {
                            return list.contains(role.getDeptId());
                        }
                        // 当前部门及该部门所有下级部门，判断是否包含当前用户部门
                        List<String> childDeptIds = TreeUtils.getListPassingBy(deptList, role.getDeptId(), Dept::getId, Dept::getParentId).stream().map(Dept::getId).collect(Collectors.toList());
                        return childDeptIds.containsAll(list);
                    })
                    .map(DeptRole::getRoleId)
                    .collect(Collectors.toList());
            if (ObjectNull.isNotNull(deptRoleIds)) {
                roleIds.addAll(deptRoleIds);
            }
        }
        return roleIds.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 批量获取用户角色
     * 包括：用户关联的角色、成员范围为”所有人“的角色、用户所属部门关联的角色
     *
     * @param userIds 用户id
     * @return Map<用户id, 角色集合>
     */
    public Map<String, List<Role>> getUserRoleIds(Collection<String> userIds) {
        // 获取用户角色
        Map<String, List<Role>> userRoleMap = userRoleService.getRoleByUserId(userIds);
        Map<String, Role> roleAllMap = roleService.list().stream().collect(Collectors.toMap(Role::getId, Function.identity()));
        // 获取成员范围是"所有人"的角色id集合
        List<Role> scopeAllRoles = roleService.getScopeAllRoles().stream().map(roleAllMap::get).collect(Collectors.toList());
        if (ObjectNull.isNotNull(scopeAllRoles)) {
            userRoleMap.forEach((key, value) -> value.addAll(scopeAllRoles));
        }
        // 获取用户所属部门相关的角色
        List<DeptRole> deptRoles = deptRoleService.list();
        if (ObjectNull.isNull(deptRoles)) {
            return userRoleMap;
        }
        Map<String, List<UserTenant>> userDeptMap = new HashMap<>();
//                userTenantService.list(Wrappers.<UserTenant>lambdaQuery()
//                        .select(UserTenant::getDeptId, UserTenant::getUserId)
//                        .in(UserTenant::getUserId, userIds))
//                .stream()
//                .filter(userTenant -> ObjectNull.isNotNull(userTenant.getDeptId()))
//                .collect(Collectors.groupingBy(UserTenant::getDeptId));
        if (ObjectNull.isNotNull(userDeptMap)) {
            List<Dept> deptList = deptService.list();
            deptRoles.forEach(deptRole -> {
                Set<String> userIdList = new HashSet<>();
                // 不包括部门的下级部门，判断部门id是否与用户部门相同
                if (Boolean.FALSE.equals(deptRole.getBelow())) {
                    List<String> deptUserIds = Optional.ofNullable(userDeptMap.get(deptRole.getDeptId())).orElseGet(ArrayList::new).stream().map(UserTenant::getUserId).collect(Collectors.toList());
                    if (ObjectNull.isNotNull(deptUserIds)) {
                        userIdList.addAll(deptUserIds);
                    }
                } else {
                    // 当前部门及该部门所有下级部门，判断是否包含当前用户部门
                    List<String> childDeptIds = TreeUtils.getListPassingBy(deptList, deptRole.getDeptId(), Dept::getId, Dept::getParentId).stream().map(Dept::getId).collect(Collectors.toList());
                    List<String> deptUserIds = childDeptIds.stream()
                            .flatMap(deptId -> Optional.ofNullable(userDeptMap.get(deptId)).orElseGet(ArrayList::new)
                                    .stream().map(UserTenant::getUserId)
                                    .collect(Collectors.toList())
                                    .stream())
                            .collect(Collectors.toList());
                    if (ObjectNull.isNotNull(deptUserIds)) {
                        userIdList.addAll(deptUserIds);
                    }
                }
                userIdList.forEach(userId -> userRoleMap.get(userId).add(roleAllMap.get(deptRole.getRoleId())));
            });
        }
        userRoleMap.entrySet().forEach(e -> e.setValue(e.getValue().stream().distinct().collect(Collectors.toList())));
        return userRoleMap;
    }

    /**
     * 移除指定用户信息
     *
     * @param userId
     */
    public void clearUser(@NotNull String userId) {
        userRoleService.clearUser(userId);
    }


    /**
     * 查询指定角色用户
     *
     * @param roleIds             角色id集合
     * @param filterScopeRoleUser true-筛选权限范围包括指定部门的角色用户id, false-不筛选
     * @param filterScopeDeptIds   筛选权限范围的条件
     * @return 用户id集合
     */
    public List<String> getRoleUserIds(List<String> roleIds, Boolean filterScopeRoleUser, List<String> filterScopeDeptIds) {
        if (ObjectNull.isNull(roleIds)) {
            return Collections.emptyList();
        }
        List<Role> roleList = roleService.listByIds(roleIds);
        // 若有角色的范围为所有人，则直接返回所有用户id
        boolean roleScopeAll = roleList.stream().anyMatch(role -> RoleMemberScopeEnum.ALL.equals(role.getMemberScope()));
        if (roleScopeAll) {
            return userService.list().stream().map(User::getId).collect(Collectors.toList());
        }

        // 查询用户-角色包含的用户id集合
        List<String> userIds = getUserIdsFromUserRole(roleList, filterScopeRoleUser, filterScopeDeptIds);
        // 查询部门-角色包含的用户id集合
        List<String> deptRoleIds = roleList.stream()
                .filter(role -> RoleMemberScopeEnum.DEPT.equals(role.getMemberScope()))
                .map(Role::getId)
                .collect(Collectors.toList());
        if (ObjectNull.isNull(deptRoleIds)) {
            return userIds;
        }
        List<DeptRole> deptRoles = deptRoleService.list(Wrappers.<DeptRole>lambdaQuery().in(DeptRole::getRoleId, deptRoleIds));
        if (ObjectNull.isNull(deptRoles)) {
            return userIds;
        }
        List<Dept> deptList = deptService.list();
        List<String> deptIds = deptRoles.stream().flatMap(deptRole -> {
            // 不包括部门的下级部门
            if (Boolean.FALSE.equals(deptRole.getBelow())) {
                return Stream.of(deptRole.getDeptId());
            } else {
                // 当前部门及该部门所有下级部门
                return TreeUtils.getListPassingBy(deptList, deptRole.getDeptId(), Dept::getId, Dept::getParentId).stream().map(Dept::getId).collect(Collectors.toList()).stream();
            }
        }).collect(Collectors.toList());
        userIds.addAll(userTenantService.list(Wrappers.<UserTenant>lambdaQuery()
                        .select(UserTenant::getUserId)
                        .in(UserTenant::getDeptId, deptIds))
                .stream()
                .map(UserTenant::getUserId)
                .collect(Collectors.toList()));
        return userIds.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 从用户角色集合中得到符合条件的用户id
     *
     * @param roleList            用户角色集合
     * @param filterScopeRoleUser true-筛选权限范围包括指定部门的角色用户id, false-不筛选
     * @param filterScopeDeptIds  筛选权限范围的条件
     * @return 用户id集合
     */
    private List<String> getUserIdsFromUserRole(List<Role> roleList, Boolean filterScopeRoleUser, List<String> filterScopeDeptIds) {
        List<String> userRoleIds = roleList.stream()
                .filter(role -> RoleMemberScopeEnum.USER.equals(role.getMemberScope()))
                .map(Role::getId)
                .collect(Collectors.toList());
        if (ObjectNull.isNull(userRoleIds)) {
            return new ArrayList<>();
        }
        List<UserRole> userRoles = userRoleService.list(Wrappers.<UserRole>lambdaQuery().in(UserRole::getRoleId, userRoleIds));
        if (ObjectNull.isNull(userRoles)) {
            return new ArrayList<>();
        }
        // 获取角色所有用户id
        if (Boolean.FALSE.equals(filterScopeRoleUser)) {
            return userRoles.stream().map(UserRole::getUserId).collect(Collectors.toList());
        }
        // 筛选权限范围包括指定部门的角色用户id
        // 有配置当前部门及下级部门的，得到筛选条件部门及其子部门
        boolean below = userRoles.stream()
                .filter(userRole -> ObjectNull.isNotNull(userRole.getScopes()))
                .anyMatch(userRole -> userRole.getScopes().stream().anyMatch(scope -> Boolean.TRUE.equals(scope.getBelow())));
        List<Dept> deptList = below ? deptService.list() : Collections.emptyList();
        return userRoles.stream()
                .filter(userRole -> {
                    // 没配置管理范围，直接返回
                    if (ObjectNull.isNull(userRole.getScopes())) {
                        return true;
                    }
                    // 筛选权限范围的条件为空，则必然不在管理范围内
                    if (ObjectNull.isNull(filterScopeDeptIds)) {
                        return false;
                    }
                    // 有配置管理范围，则判断部门是否在范围内
                    return userRole.getScopes()
                            .stream()
                            .anyMatch(scope -> {
                                if (scope.getBelow()) {
                                    List<String> selfAndChildrenDeptIds = TreeUtils.getListPassingBy(deptList, scope.getDeptId(), Dept::getId, Dept::getParentId)
                                            .stream()
                                            .map(Dept::getId)
                                            .collect(Collectors.toList());
                                    return !Collections.disjoint(selfAndChildrenDeptIds, filterScopeDeptIds);
                                } else {
                                    return filterScopeDeptIds.contains(scope.getDeptId());
                                }
                            });
                })
                .map(UserRole::getUserId)
                .collect(Collectors.toList());
    }
}
