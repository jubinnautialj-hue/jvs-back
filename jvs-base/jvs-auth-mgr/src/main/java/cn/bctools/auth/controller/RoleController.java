package cn.bctools.auth.controller;

import cn.bctools.auth.api.enums.PersonnelTypeEnum;
import cn.bctools.auth.component.UserRoleComponent;
import cn.bctools.auth.entity.*;
import cn.bctools.auth.entity.po.UserRoleScope;
import cn.bctools.auth.service.*;
import cn.bctools.auth.vo.RoleDeptVo;
import cn.bctools.auth.vo.RoleUserVo;
import cn.bctools.auth.vo.UserRoleScopeVo;
import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.entity.po.TreePo;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.*;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.config.JvsOAuth2AuthorizationServiceImpl;
import cn.bctools.oauth2.dto.CustomUser;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 角色接口
 *
 * @author Administrator
 * @Description: 角色接口
 */
@Slf4j
@AllArgsConstructor
@Api(tags = "角色接口")
@RestController
@RequestMapping("role")
public class RoleController {

    UserService userService;
    RoleService roleService;
    JvsOAuth2AuthorizationServiceImpl jvsOAuth2AuthorizationService;
    RedisUtils redisUtils;
    UserRoleService userRoleService;
    UserRoleComponent userRoleComponent;
    DeptService deptService;
    DeptRoleService deptRoleService;
    UserTenantService userTenantService;
    PermissionService permissionService;
    RolePermissionService rolePermissionService;
    RoleGroupService roleGroupService;

    @Log(back = false)
    @GetMapping("/all")
    @ApiOperation(value = "用户角色", notes = "角色管理, 左侧的角色列表页, 默认显示所有角色, 当前用户可查看的角色都可以看")
    public R<List<Role>> userRole() {
        List<Role> list = roleService.list(Wrappers.<Role>lambdaQuery()
                .orderByDesc(Role::getCreateTime));
        return R.ok(list);
    }

    @Log(back = false)
    @GetMapping("/all/group")
    @ApiOperation(value = "获取角色和分组信息", notes = "角色管理, 左侧的角色列表页, 默认显示所有角色, 当前用户可查看的角色都可以看")
    public R<List<Tree<Object>>> userRoleGroup() {
        List<TreePo> treePos = new ArrayList<>();
        roleGroupService.list()
                .stream()
                .map(e -> new TreePo().setId(e.getId()).setName(e.getName()).setParentId(TreeUtils.DICT_ID_ROOT).setExtend(new HashMap<String, String>() {{
                    put("type", "group");
                }}))
                .forEach(treePos::add);
        roleService.list()
                .stream()
                .map(e -> {
                    Map<String, Object> extend = BeanCopyUtil.beanToMap(e);
                    extend.put("type", "role");
                    return new TreePo().setId(e.getId()).setName(e.getRoleName()).setExtend(extend).setParentId(ObjectNull.isNull(e.getRoleGroupId()) ? TreeUtils.DICT_ID_ROOT : e.getRoleGroupId());
                })
                .forEach(treePos::add);
        List<Tree<Object>> tree = TreeUtils.tree(treePos);
        return R.ok(tree);
    }

    @Log(back = false)
    @GetMapping("/group")
    @ApiOperation(value = "获取所有的角色分组")
    public R groupAll() {
        List<RoleGroup> list = roleGroupService.list();
        return R.ok(list);
    }

    @Log(back = false)
    @PostMapping("/group")
    @ApiOperation(value = "新增用户分组", notes = "角色管理, 左侧的角色列表页, 默认显示所有角色, 当前用户可查看的角色都可以看")
    public R roleGroup(@RequestBody RoleGroup roleGroup) {
        roleGroupService.saveOrUpdate(roleGroup);
        return R.ok(roleGroup);
    }

    @Log(back = false)
    @DeleteMapping("/group/{id}")
    @ApiOperation(value = "删除角色分组", notes = "角色管理, 左侧的角色列表页, 默认显示所有角色, 当前用户可查看的角色都可以看")
    @Transactional
    public R roleGroup(@PathVariable("id") String id) {
        roleGroupService.removeById(id);
        roleService.update(new Role().setRoleGroupId(TreeUtils.DICT_ID_ROOT), new LambdaQueryWrapper<Role>().eq(Role::getRoleGroupId, id));
        return R.ok();
    }


    @Log
    @PostMapping("/save")
    @ApiOperation(value = "新增角色", notes = "此功能包含有权限操作, 如果没有新增角色")
    public R<Boolean> saveRole(@RequestBody Role role) {
        //判断一个名称是否重复, 保证名称不能重复
        Role one = roleService.getOne(Wrappers.query(new Role().setRoleName(role.getRoleName())));
        if (ObjectUtil.isNotEmpty(one)) {
            return R.failed("角色名称已经存在");
        }
        roleService.save(role);
        return R.ok(true, "新增成功");
    }

    @Log
    @PutMapping("/update")
    @ApiOperation(value = "修改角色", notes = "只是修改角色基本信息")
    public R<Boolean> updateRole(@RequestBody Role role) {
        Role oldRole = roleService.getById(role.getId());
        // 若成员范围改变，则删除已有数据
        if (Boolean.FALSE.equals(oldRole.getMemberScope().equals(role.getMemberScope()))) {
            userRoleService.clearRoleUser(role.getId());
            deptRoleService.clearRoleDept(role.getId());
        }
        roleService.updateById(role);
        refresh(role.getId());
        return R.ok(true, "修改成功");
    }

    @Log
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除角色", notes = "删除后,中间关联表也会删除, 用户对应的角色信息也会没有")
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = SysConstant.CACHE_ROLE, allEntries = true)
    public R<Boolean> removeRole(@PathVariable String id) {
        //判断是否有用户
        long count = userRoleService.count(Wrappers.query(new UserRole().setRoleId(id)));
        if (count > 0) {
            return R.failed("角色下有用户不能删除");
        }
        //角色角色
        roleService.removeById(id);
        //用户角色
        //部门角色
        deptRoleService.remove(Wrappers.<DeptRole>lambdaQuery().eq(DeptRole::getRoleId, id));
        //删除角色下的资源
        rolePermissionService.remove(Wrappers.query(new RolePermission().setRoleId(id)));
        return R.ok(true, "删除成功");
    }

    @Log
    @GetMapping("/user")
    @ApiOperation(value = "用户管理", notes = "角色管理-用户管理, 对用户可实现操作, 当前角色下有哪些用户, 可对用户进行移出,系统默认有游客角色,创建用户时, 用户默认为游客角色, 角色不能删除")
    public R<Page<RoleUserVo>> user(@RequestParam(value = "roleId", required = false) String roleId, PageDTO<UserRole> page) {
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        List<String> delIds = userTenantService.list(new LambdaQueryWrapper<UserTenant>().eq(UserTenant::getCancelFlag, true).select(UserTenant::getUserId)).stream().map(e -> e.getUserId()).collect(Collectors.toList());
        if (ObjectNull.isNotNull(roleId)) {
            //排除已经删除的用户
            userRoleService.page(page, new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, roleId).notIn(ObjectNull.isNotNull(delIds), UserRole::getUserId, delIds));
        } else {
            userRoleService.page(page);
        }
        Set<String> ids = page.getRecords().stream().map(UserRole::getUserId).collect(Collectors.toSet());
        Page<RoleUserVo> userPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal(), page.isSearchCount());
        //可能没有用户
        if (ObjectUtil.isEmpty(ids)) {
            return R.ok(userPage);
        }
        // 封装响应
        Map<String, UserTenant> userTenantMap = userTenantService.list(new LambdaQueryWrapper<UserTenant>().eq(UserTenant::getTenantId, currentUser.getTenantId())
                .eq(UserTenant::getCancelFlag, false)
                .in(UserTenant::getUserId, ids)).stream().collect(Collectors.toMap(UserTenant::getUserId, Function.identity()));
        Map<String, Dept> deptMap = deptService.list().stream().collect(Collectors.toMap(Dept::getId, Function.identity()));
        Map<String, User> userMap = userService.listByIds(ids).stream().collect(Collectors.toMap(User::getId, Function.identity()));
        List<RoleUserVo> collect = page.getRecords().stream()
                .map(e -> {
                    List<UserRoleScopeVo> userRoleScopeVos = Optional.ofNullable(e.getScopes())
                            .orElseGet(ArrayList::new)
                            .stream()
                            .map(scope ->
                                    new UserRoleScopeVo()
                                            .setId(scope.getDeptId())
                                            .setBelow(scope.getBelow())
                                            .setType(PersonnelTypeEnum.dept)
                                            .setName(Optional.ofNullable(deptMap.get(scope.getDeptId())).map(Dept::getName).orElse(scope.getDeptId())))
                            .collect(Collectors.toList());
                    return BeanCopyUtil.copy(RoleUserVo.class, userMap.get(e.getUserId()), userTenantMap.get(e.getUserId())).setScopes(userRoleScopeVos);
                })
                .peek(e -> {
                    if (ObjectNull.isNotNull(e.getDeptId())) {
                        List<String> name = e.getDeptId().stream().filter(deptMap::containsKey)
                                .filter(v -> ObjectNull.isNotNull(deptMap.get(v).getName()))
                                .map(v -> deptMap.get(v).getName()).collect(Collectors.toList());
                        e.setDeptName(name);
                    }
                })
                .filter(e -> ObjectNull.isNotNull(e.getAccountName()))
                .collect(Collectors.toList());
        //角色下的用户操作
        userPage.setRecords(collect);
        return R.ok(userPage);
    }

    @Log
    @DeleteMapping("/user/{roleId}/{userId}")
    @CacheEvict(value = SysConstant.CACHE_ROLE, allEntries = true)
    @ApiOperation(value = "移出用户", notes = "角色管理-移出用户, 对用户移出这个角色")
    public R<Boolean> deleteUser(@PathVariable String roleId, @PathVariable String userId) {
        //将某个用户移出某个角色
        userRoleService.remove(Wrappers.query(new UserRole().setUserId(userId).setRoleId(roleId)));
        refresh(roleId);
        return R.ok(true, "移出成功");
    }

    @Log
    @PutMapping("/user/{roleId}")
    @ApiOperation(value = "用户管理", notes = "角色管理-添加用户,添加一些用户到此角色下")
    @CacheEvict(value = SysConstant.CACHE_ROLE, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> saveUser(@PathVariable String roleId, @RequestBody List<String> userIds) {
        if (ObjectNull.isNull(userIds)) {
            return R.ok();
        }
        // 将这些用户排除保存用户
        List<String> dbUserIds = userRoleService.list(Wrappers.<UserRole>lambdaQuery().select(UserRole::getUserId)
                        .eq(UserRole::getRoleId, roleId)
                        .in(UserRole::getUserId, userIds))
                .stream().map(UserRole::getUserId)
                .collect(Collectors.toList());
        // 排除重复的角色用户
        userIds.removeAll(dbUserIds);
        //将某个用户添加到某个角色下
        String tenantId = UserCurrentUtils.getCurrentUser().getTenantId();
        Set<UserRole> collect = userIds.stream()
                .map(e -> new UserRole().setUserId(e).setRoleId(roleId).setTenantId(tenantId))
                .collect(Collectors.toSet());
        userRoleService.saveBatch(collect);
        refresh(roleId);
        return R.ok(true, "添加成功");
    }

    @Log
    @PutMapping("/user/scope")
    @ApiOperation(value = "用户管理", notes = "角色管理-设置管理范围")
    @CacheEvict(value = SysConstant.CACHE_ROLE, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> saveUserScope(@Validated @RequestBody UserRole req) {
        UserRole userRole = Optional.ofNullable(userRoleService.getOne(Wrappers.<UserRole>lambdaQuery()
                        .eq(UserRole::getRoleId, req.getRoleId())
                        .eq(UserRole::getUserId, req.getUserId())))
                .orElseThrow(() -> new BusinessException("角色不存在"));
        userRole.setScopes(req.getScopes());
        userRoleService.update(userRole, Wrappers.<UserRole>lambdaUpdate()
                .eq(UserRole::getRoleId, req.getRoleId())
                .eq(UserRole::getUserId, req.getUserId()));
        return R.ok();
    }


    @Log(back = false)
    @GetMapping("/role/{roleId}")
    @ApiOperation(value = "获取角色的菜单权限")
    public R<List<String>> getRole(@PathVariable String roleId) {
        //判断角色是租户角色，还是用户角色
        Role role = roleService.getById(roleId);
        if (ObjectNull.isNull(role)) {
            return R.failed("角色不存在");
        }
        List<String> list = rolePermissionService.list(Wrappers.query(new RolePermission().setRoleId(roleId)))
                .stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());
        return R.ok(list);
    }

    @Log
    @PutMapping("/role/{roleId}")
    @ApiOperation(value = "角色授权", notes = "给角色授权, 不管是组织角色, 还是用户角色, 都使用同一个授权接口,根据角色内容, 控制授权信息, 如果是组织角色, 则归属于组织, 如果是用户角色, 保持与用户相同操作")
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = SysConstant.CACHE_ROLE, allEntries = true)
    public R<Boolean> tenantRole(@PathVariable("roleId") String roleId, @RequestBody List<String> list) {
        // 删除后重新添加权限
        rolePermissionService.remove(Wrappers.query(new RolePermission().setRoleId(roleId)));
        List<RolePermission> collect = list.stream().map(e -> new RolePermission().setRoleId(roleId).setPermissionId(e)).collect(Collectors.toList());
        rolePermissionService.saveBatch(collect);
        refresh(roleId);
        return R.ok(true, "授权成功");
    }

    @Log
    @GetMapping("/dept")
    @ApiOperation(value = "角色部门", notes = "角色管理-部门, 对部门可实现操作, 当前角色下有哪些部门, 可对部门进行移出")
    public R<Page<RoleDeptVo>> dept(@RequestParam(value = "roleId", defaultValue = "") String roleId, PageDTO<DeptRole> page) {
        if (ObjectNull.isNull(roleId)) {
            return R.ok(null);
        }
        deptRoleService.page(page, Wrappers.query(new DeptRole().setRoleId(roleId)));
        Set<String> ids = page.getRecords().stream().map(DeptRole::getDeptId).collect(Collectors.toSet());
        Page<RoleDeptVo> deptPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal(), page.isSearchCount());
        //可能没有部门
        if (ObjectUtil.isEmpty(ids)) {
            return R.ok(deptPage);
        }
        Map<String, Dept> deptMap = deptService.listByIds(ids).stream().collect(Collectors.toMap(Dept::getId, Function.identity()));
        List<RoleDeptVo> collect = page.getRecords().stream().map(e -> {
            RoleDeptVo copy = BeanCopyUtil.copy(e, RoleDeptVo.class);
            if (deptMap.containsKey(e.getDeptId())) {
                copy.setName(deptMap.get(e.getDeptId()).getName());
            }
            return copy;
        }).collect(Collectors.toList());
        //角色下的用户操作
        deptPage.setRecords(collect);
        return R.ok(deptPage);
    }

    @Log
    @DeleteMapping("/dept/{roleId}/{deptId}")
    @CacheEvict(value = SysConstant.CACHE_ROLE, allEntries = true)
    @ApiOperation(value = "移出部门", notes = "角色管理-移出部门, 对部门移出这个角色")
    public R<Boolean> deleteDept(@PathVariable String roleId, @PathVariable String deptId) {
        //将某个部门移出某个角色
        deptRoleService.remove(Wrappers.query(new DeptRole().setDeptId(deptId).setRoleId(roleId)));
        return R.ok();
    }

    @Log
    @PostMapping("/dept/{roleId}")
    @ApiOperation(value = "角色部门", notes = "角色管理-添加部门,添加一些部门到此角色下")
    @CacheEvict(value = SysConstant.CACHE_ROLE, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> addDept(@PathVariable String roleId, @RequestBody List<String> deptIds) {
        // 排除重复的部门
        List<String> dbDeptIds = deptRoleService.list(Wrappers.<DeptRole>lambdaQuery().select(DeptRole::getDeptId)
                        .eq(DeptRole::getRoleId, roleId)
                        .in(DeptRole::getDeptId, deptIds))
                .stream().map(DeptRole::getDeptId)
                .collect(Collectors.toList());
        deptIds.removeAll(dbDeptIds);
        //将某个部门添加到某个角色下
        String tenantId = UserCurrentUtils.getCurrentUser().getTenantId();
        Set<DeptRole> collect = deptIds.stream()
                .distinct()
                .map(e -> new DeptRole().setDeptId(e).setBelow(Boolean.FALSE).setRoleId(roleId).setTenantId(tenantId))
                .collect(Collectors.toSet());
        deptRoleService.saveBatch(collect);
        return R.ok();
    }

    @Log
    @PutMapping("/dept/{roleId}")
    @ApiOperation(value = "角色部门", notes = "角色管理-修改部门")
    public R<Boolean> updateDept(@PathVariable String roleId, @RequestBody List<DeptRole> depts) {
        Optional.ofNullable(roleService.getById(roleId)).orElseThrow(() -> new BusinessException("角色不存在"));
        String tenantId = UserCurrentUtils.getCurrentUser().getTenantId();
        if (ObjectNull.isNotNull(depts)) {
            depts.forEach(e -> {
                deptRoleService.update(e, Wrappers.query(new DeptRole().setRoleId(roleId).setDeptId(e.getDeptId()).setTenantId(tenantId)));
            });
        }
        return R.ok();
    }

    /**
     * 刷新缓存
     */
    public void refresh(String roleId) {
        //强制刷新当前登录用户的权限
        ArrayList<String> objects = new ArrayList<>();
        objects.add(roleId);
        List<String> roleUserIds = userRoleComponent.getRoleUserIds(objects, false, null);
        String tenantId = TenantContextHolder.getTenantId();
        Set<String> keys = redisUtils.keys("jvs:token:" + tenantId + "*");
        keys.parallelStream()
                .forEach(e -> {
                    try {
                        String jvs = e.toString().substring(4);
                        List o = (List) redisUtils.get(jvs);
                        Object o1 = o.get(1);
                        OAuth2Authorization byToken = jvsOAuth2AuthorizationService.findByToken(o1.toString().replaceAll(OAuth2TokenType.REFRESH_TOKEN.getValue().toLowerCase(), ""), OAuth2TokenType.REFRESH_TOKEN);
                        CustomUser user = byToken.getAttribute("user");
                        if (roleUserIds.contains(user.getUserDto().getId())) {
                            //如果用户是匹配的，就删除这个 Key
                            redisUtils.del(OAuth2ParameterNames.ACCESS_TOKEN + ":" + byToken.getAccessToken().getToken().getTokenValue());
                        }
                    } catch (Exception ex) {
                    }
                });
    }
}
