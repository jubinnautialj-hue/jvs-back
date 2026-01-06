package cn.bctools.design.permission;

import cn.bctools.auth.api.api.AuthDeptServiceApi;
import cn.bctools.auth.api.api.AuthRoleServiceApi;
import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.auth.api.dto.SysDeptDto;
import cn.bctools.auth.api.dto.SysRoleDto;
import cn.bctools.auth.api.enums.PersonnelTypeEnum;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TreeUtils;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.menu.component.AppVersionMenuHandler;
import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.menu.entity.AppMenuType;
import cn.bctools.design.menu.entity.dto.PermissionIdentificationDto;
import cn.bctools.design.menu.util.DesignPermissionUtil;
import cn.bctools.design.menu.util.JvsMenuVo;
import cn.bctools.design.menu.util.MenuUtil;
import cn.bctools.design.permission.dto.*;
import cn.bctools.design.permission.entity.PermissionGroup;
import cn.bctools.design.permission.entity.PermissionSetting;
import cn.bctools.design.permission.entity.dto.PermissionMemberDto;
import cn.bctools.design.permission.entity.enums.PermissionGroupType;
import cn.bctools.design.permission.service.DesignPermissionService;
import cn.bctools.design.permission.service.PermissionGroupService;
import cn.bctools.design.permission.service.PermissionSettingService;
import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Api(tags = "[design]统一权限管理")
@RestController
@AllArgsConstructor
@RequestMapping("/app/design/{appId}/permission")
public class PermissionController {

    private final PermissionGroupService permissionGroupService;
    private final PermissionSettingService permissionSettingService;
    private final DesignPermissionService designPermissionService;
    private final CrudPageService pageService;
    private final AppVersionMenuHandler appVersionMenuHandler;
    private final AuthDeptServiceApi deptServiceApi;
    private final AuthRoleServiceApi roleServiceApi;
    private final AuthUserServiceApi userServiceApi;

    @ApiOperation("查询权限组集合")
    @GetMapping("/group/list/{groupType}")
    public R<List<PermissionGroup>> groupList(@PathVariable String appId, @PathVariable PermissionGroupType groupType) {
        List<PermissionGroup> groups = permissionGroupService.list(Wrappers.<PermissionGroup>lambdaQuery()
                .eq(PermissionGroup::getJvsAppId, appId)
                .eq(PermissionGroup::getGroupType, groupType)
                .orderByAsc(PermissionGroup::getCreateTime));
        List<String> userIds = new ArrayList<>();
        List<String> deptIds = new ArrayList<>();
        List<String> roleIds = new ArrayList<>();
        for (PermissionGroup group : groups) {
            if (ObjectNull.isNull(group.getMember())) {
                continue;
            }
            PermissionMemberDto member = group.getMember();
            if (ObjectNull.isNull(member.getPersonnels())) {
                continue;
            }
            member.getPersonnels().forEach(personnel -> {
                if (PersonnelTypeEnum.user.equals(personnel.getType())) {
                    userIds.add(personnel.getId());
                }
                if (PersonnelTypeEnum.dept.equals(personnel.getType())) {
                    deptIds.add(personnel.getId());
                }
                if (PersonnelTypeEnum.role.equals(personnel.getType())) {
                    roleIds.add(personnel.getId());
                }
            });
        }
        Map<String, String> userMap = ObjectNull.isNull(userIds) ? Collections.emptyMap() : userServiceApi.getByIds(userIds).getData().stream()
                .collect(Collectors.toMap(UserDto::getId, UserDto::getRealName));
        Map<String, String> roleMap = ObjectNull.isNull(roleIds) ? Collections.emptyMap() : roleServiceApi.getByIds(roleIds).getData().stream()
                .collect(Collectors.toMap(SysRoleDto::getId, SysRoleDto::getRoleName));
        Map<String, String> deptMap =  ObjectNull.isNull(deptIds) ? Collections.emptyMap(): deptServiceApi.getByIds(deptIds).getData().stream()
                .collect(Collectors.toMap(SysDeptDto::getId, SysDeptDto::getName));

        groups.forEach(group -> {
            PermissionMemberDto member = group.getMember();
            if (ObjectNull.isNotNull(member) && ObjectNull.isNotNull(member.getPersonnels())) {
               member.getPersonnels().forEach(personnel -> {
                   if (PersonnelTypeEnum.user.equals(personnel.getType())) {
                       personnel.setName(userMap.getOrDefault(personnel.getId(), personnel.getName()));
                   }
                   if (PersonnelTypeEnum.dept.equals(personnel.getType())) {
                       personnel.setName(deptMap.getOrDefault(personnel.getId(), personnel.getName()));
                   }
                   if (PersonnelTypeEnum.role.equals(personnel.getType())) {
                       personnel.setName(roleMap.getOrDefault(personnel.getId(), personnel.getName()));
                   }
               });
            }
        });
        

        return R.ok(groups);
    }

    @ApiOperation("新增权限组基本信息")
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/group")
    public R createGroup(@Validated @RequestBody CreatePermissionGroupReqDto dto, @PathVariable String appId) {
        PermissionGroup permissionGroup = BeanCopyUtil.copy(dto, PermissionGroup.class);
        permissionGroup.setJvsAppId(appId);
        permissionGroupService.save(permissionGroup);
        return R.ok();
    }

    @ApiOperation("修改权限组基本信息")
    @Transactional(rollbackFor = Exception.class)
    @PutMapping("/group")
    public R updateGroup(@Validated @RequestBody UpdatePermissionGroupReqDto dto, @PathVariable String appId) {
        PermissionGroup permissionGroup = BeanCopyUtil.copy(dto, PermissionGroup.class);
        permissionGroup.setJvsAppId(appId);
        permissionGroupService.updateById(permissionGroup);
        return R.ok();
    }

    @ApiOperation("删除权限组")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupId", value = "权限组id", required = true)
    })
    @DeleteMapping("/group/{groupId}")
    public R<String> deleteGroup(@PathVariable String groupId, @PathVariable String appId) {
        // 删除权限组
        permissionGroupService.removeById(groupId);
        // 删除权限组权限配置
        permissionSettingService.removeByGroup(groupId);
        return R.ok();
    }


    @ApiOperation("查询权限组的权限配置")
    @GetMapping("/{groupId}")
    public R<List<PermissionSettingDto>> groupPermission(@PathVariable String groupId, @PathVariable String appId) {
        List<PermissionSettingDto> permissionSettings = Optional.ofNullable(permissionSettingService.list(Wrappers.<PermissionSetting>lambdaQuery()
                        .eq(PermissionSetting::getPermissionGroupId, groupId))).orElseGet(ArrayList::new)
                .stream()
                .map(p ->
                        new PermissionSettingDto()
                                .setDesignId(p.getDesignId())
                                .setPermission(p.getPermission()))
                .collect(Collectors.toList());
        return R.ok(permissionSettings);
    }

    @ApiOperation(value = "保存权限设置", notes = "保存设计的权限")
    @Transactional(rollbackFor = Exception.class)
    @PutMapping("/{groupId}")
    public R savePermission(@Validated @RequestBody SavePermissionReqDto dto, @PathVariable String groupId, @PathVariable String appId) {
        designPermissionService.savePermission(appId, groupId, dto.getMember(), dto.getPermissions());
        return R.ok();
    }

    @ApiOperation(value = "保存模型权限设置")
    @Transactional(rollbackFor = Exception.class)
    @PutMapping("/model/{groupId}")
    public R saveModelPermission(@Validated @RequestBody SavePermissionReqDto dto, @PathVariable String groupId, @PathVariable String appId) {
        designPermissionService.savePermission(appId, groupId, dto.getMember(), dto.getPermissions());
        return R.ok();
    }

    @ApiOperation(value = "保存自定义页面操作权限标识")
    @Transactional(rollbackFor = Exception.class)
    @PutMapping("/url/operation/{designId}")
    public R saveAppUrlOperation(@PathVariable String appId, @PathVariable String designId, @RequestBody List<PermissionEndpoint> operations) {
        PermissionIdentificationDto permissionIdentification = new PermissionIdentificationDto();
        permissionIdentification.setUrlOperation(operations);
        appVersionMenuHandler.updateOperation(designId, permissionIdentification);
        return R.ok();
    }

    @ApiOperation(value = "获取应用设计操作权限标识", notes = "设计的操作权限标识，菜单排在前面（菜单同模型的设计与菜单一组）；非菜单且与菜单模型不同的设计作为一组")
    @GetMapping("/operation")
    public R<List<DesignPermissionDto>> getPermission(@PathVariable("appId") String appId) {
        log.info("[权限操作接口] 开始查询应用权限标识，appId={}", appId);
        
        List<AppMenu> appMenus = appVersionMenuHandler.getAppVersionMenu(appId);
        log.info("[权限操作接口] 查询到的应用菜单列表，appId={}, 菜单数量={}", appId, appMenus != null ? appMenus.size() : 0);
        if (appMenus == null || appMenus.isEmpty()) {
            log.warn("[权限操作接口] 应用菜单列表为空，appId={}", appId);
        }
        
        // 菜单
        List<AppMenu> menus = appMenus.stream()
                .filter(d -> ObjectNull.isNotNull(d.getType()))
                .collect(Collectors.toList());
        log.info("[权限操作接口] 菜单类型数据筛选，appId={}, 菜单数量={}", appId, menus.size());
        
        // 非菜单 （只要表单、列表）
        List<AppMenu> notMenus = appMenus.stream()
                .filter(d -> ObjectNull.isNull(d.getType()))
                .filter(d -> DesignType.page.equals(d.getDesignType()) || DesignType.form.equals(d.getDesignType()))
                .collect(Collectors.toList());
        log.info("[权限操作接口] 非菜单类型数据筛选，appId={}, 非菜单数量={}", appId, notMenus.size());

        // 组装渲染结构
        List<DesignPermissionDto> designPermissions = new ArrayList<>();
        // 查询菜单并按菜单顺序排序
        List<AppMenuType> menuTypes = appVersionMenuHandler.getAppVersionMenuType(appId);
        log.info("[权限操作接口] 查询菜单类型，appId={}, 菜单类型数量={}", appId, menuTypes != null ? menuTypes.size() : 0);
        
        List<JvsMenuVo> menuTypelist = menuTypes.stream()
                .map(menuType -> (JvsMenuVo)new JvsMenuVo().setId(menuType.getId()).setName(menuType.getType()).setSort(menuType.getSort()).setParentId(appId))
                .collect(Collectors.toList());
        List<JvsMenuVo> menulist = menus.stream()
                .map(menu -> (JvsMenuVo)new JvsMenuVo().setId(menu.getDesignId()).setName(menu.getName()).setSort(menu.getSort()).setParentId(menu.getType()))
                .collect(Collectors.toList());
        List<JvsMenuVo> menuAll = new ArrayList<>();
        menuAll.add((JvsMenuVo)new JvsMenuVo().setJvsAppId(appId).setId(appId).setParentId("-1"));
        menuAll.addAll(menuTypelist);
        menuAll.addAll(menulist);
        List<Tree<Object>> tree = JvsMenuVo.tree(menuAll);
        log.info("[权限操作接口] 构建菜单树结构，appId={}, 树节点数量={}", appId, tree != null ? tree.size() : 0);
        
        if (tree == null || tree.isEmpty()) {
            log.warn("[权限操作接口] 菜单树为空，appId={}, 返回空结果", appId);
            return R.ok(designPermissions);
        }
        
        List<Tree<Object>> ts = TreeUtils.tree2List(tree.get(0), Tree::getChildren);
        log.info("[权限操作接口] 菜单树展平，appId={}, 展平后节点数量={}", appId, ts.size());
        
        List<String> menuIds = menus.stream().map(AppMenu::getDesignId).collect(Collectors.toList());
        List<String> treeIds = ts.stream().map(m->String.valueOf(m.getId())).collect(Collectors.toList());
        Map<String, AppMenu> menuMap = menus.stream().collect(Collectors.toMap(AppMenu::getDesignId, Function.identity()));
        log.info("[权限操作接口] 处理菜单ID列表，appId={}, menuIds数量={}, treeIds数量={}", appId, menuIds.size(), treeIds.size());
        
        for (String menuId : treeIds) {
            if (Boolean.FALSE.equals(menuIds.contains(menuId))) {
                continue;
            }
            // 组装菜单资源信息
            AppMenu menu = menuMap.get(menuId);
            log.debug("[权限操作接口] 处理菜单，appId={}, menuId={}, menuName={}", appId, menuId, menu != null ? menu.getName() : "null");
            PermissionIdentificationDto menuPermission = DesignPermissionUtil.parseDesign(menu);
            DesignPermissionDto menuDesignPermission = new DesignPermissionDto()
                    .setName(menu.getName())
                    .setId(menu.getDesignId())
                    .setDesignType(menu.getDesignType())
                    .setOperation(menuPermission.getOperation())
                    .setTreeOperation(menuPermission.getTreeOperation())
                    .setUrlOperation(menuPermission.getUrlOperation());

            String dataModelId = menu.getDataModelId();
            if (ObjectNull.isNull(dataModelId)) {
                designPermissions.add(menuDesignPermission);
                continue;
            }
            // 模型id不为空，则组装菜单同模型的设计
            List<AppMenu> sameModelDesigns = notMenus.stream()
                    .filter(n -> dataModelId.equals(n.getDataModelId()))
                    .collect(Collectors.toList());
            List<DesignPermissionDto> relevantList = relevant(sameModelDesigns);
            menuDesignPermission.setRelevant(relevantList);
            designPermissions.add(menuDesignPermission);
            notMenus.removeIf(design -> sameModelDesigns.stream().anyMatch(d -> design.getDesignId().equals(d.getDesignId())));
        }
        // 剩下与菜单不同模型的设计为一个组
        DesignPermissionDto menuDesignPermission = new DesignPermissionDto();
        List<DesignPermissionDto> notSameModelRelevantList = relevant(notMenus);
        log.info("[权限操作接口] 处理非同模型设计，appId={}, 非同模型设计数量={}", appId, notSameModelRelevantList != null ? notSameModelRelevantList.size() : 0);
        
        if (ObjectNull.isNotNull(notSameModelRelevantList)) {
            menuDesignPermission.setRelevant(notSameModelRelevantList);
            designPermissions.add(menuDesignPermission);
        }
        
        log.info("[权限操作接口] 查询完成，appId={}, 最终返回权限数量={}", appId, designPermissions.size());
        if (designPermissions.isEmpty()) {
            log.warn("[权限操作接口] 返回结果为空，appId={}", appId);
        }
        
        return R.ok(designPermissions);
    }

    private List<DesignPermissionDto> relevant(List<AppMenu> designs) {
        return designs.stream().map(design -> {
            PermissionIdentificationDto permission = DesignPermissionUtil.parseDesign(design);
            return new DesignPermissionDto()
                    .setId(design.getDesignId())
                    .setName(design.getName())
                    .setDesignType(design.getDesignType())
                    .setOperation(permission.getOperation())
                    .setTreeOperation(permission.getTreeOperation())
                    .setUrlOperation(permission.getUrlOperation());
        }).collect(Collectors.toList());
    }


    @ApiOperation(value = "获取应用所有列表设计", notes = "用以渲染统一分配数据权限")
    @GetMapping("/page")
    public R<List<CrudPage>> crudPage(@PathVariable("appId") String appId) {
        // 获取所有列表设计
        Map<String, CrudPage> pageMap = pageService.list(Wrappers.<CrudPage>lambdaQuery()
                        .select(CrudPage::getId, CrudPage::getName, CrudPage::getDataModelId)
                        .isNotNull(CrudPage::getName)
                        .eq(CrudPage::getJvsAppId, appId))
                .stream()
                .collect(Collectors.toMap(CrudPage::getId, Function.identity()));
        if (MapUtils.isEmpty(pageMap)) {
            return R.ok();
        }
        // 查询列表类型的菜单（并按菜单顺序排序）
        List<AppMenuType> menuTypes = appVersionMenuHandler.getAppVersionMenuType(appId);
        List<AppMenu> appMenus = appVersionMenuHandler.getAppVersionMenu(appId).stream()
                .filter(m -> DesignType.page.equals(m.getDesignType()) && ObjectNull.isNotNull(m.getType()))
                .collect(Collectors.toList());
        List<String> menuPageIds = MenuUtil.getMenuIds(menuTypes, appMenus);
        // 菜单列表排在前面，非菜单列表排在后面
        List<CrudPage> crudPages = menuPageIds.stream().map(pageMap::get).collect(Collectors.toList());
        List<CrudPage> notMenuPages = pageMap.entrySet().stream().filter(e -> Boolean.FALSE.equals(menuPageIds.contains(e.getKey()))).map(Map.Entry::getValue).collect(Collectors.toList());
        crudPages.addAll(notMenuPages);
        return R.ok(crudPages);
    }
}
