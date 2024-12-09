package cn.bctools.design.permission;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @ApiOperation("查询权限组集合")
    @GetMapping("/group/list/{groupType}")
    public R<List<PermissionGroup>> groupList(@PathVariable String appId, @PathVariable PermissionGroupType groupType) {
        return R.ok(permissionGroupService.list(Wrappers.<PermissionGroup>lambdaQuery()
                .eq(PermissionGroup::getJvsAppId, appId)
                .eq(PermissionGroup::getGroupType, groupType)
                .orderByAsc(PermissionGroup::getCreateTime)));
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
    public R saveAppUrlOperation(@PathVariable String appId, @PathVariable String designId, @RequestBody List<Map<String, String>> operations) {
        PermissionIdentificationDto permissionIdentification = new PermissionIdentificationDto();
        permissionIdentification.setUrlOperation(operations);
        appVersionMenuHandler.updateOperation(designId, permissionIdentification);
        return R.ok();
    }

    @ApiOperation(value = "获取应用设计操作权限标识", notes = "设计的操作权限标识，菜单排在前面（菜单同模型的设计与菜单一组）；非菜单且与菜单模型不同的设计作为一组")
    @GetMapping("/operation")
    public R<List<DesignPermissionDto>> getPermission(@PathVariable("appId") String appId) {
        List<AppMenu> appMenus = appVersionMenuHandler.getAppVersionMenu(appId);
        // 菜单
        List<AppMenu> menus = appMenus.stream()
                .filter(d -> ObjectNull.isNotNull(d.getType()))
                .collect(Collectors.toList());
        // 非菜单 （只要表单、列表）
        List<AppMenu> notMenus = appMenus.stream()
                .filter(d -> ObjectNull.isNull(d.getType()))
                .filter(d -> DesignType.page.equals(d.getDesignType()) || DesignType.form.equals(d.getDesignType()))
                .collect(Collectors.toList());

        // 组装渲染结构
        List<DesignPermissionDto> designPermissions = new ArrayList<>();
        // 查询菜单并按菜单顺序排序
        List<AppMenuType> menuTypes = appVersionMenuHandler.getAppVersionMenuType(appId);
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
        List<Tree<Object>> ts = TreeUtils.tree2List(tree.get(0), Tree::getChildren);
        List<String> menuIds = menus.stream().map(AppMenu::getDesignId).collect(Collectors.toList());
        List<String> treeIds = ts.stream().map(m->String.valueOf(m.getId())).collect(Collectors.toList());
        Map<String, AppMenu> menuMap = menus.stream().collect(Collectors.toMap(AppMenu::getDesignId, Function.identity()));
        for (String menuId : treeIds) {
            if (Boolean.FALSE.equals(menuIds.contains(menuId))) {
                continue;
            }
            // 组装菜单资源信息
            AppMenu menu = menuMap.get(menuId);
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
        if (ObjectNull.isNotNull(notSameModelRelevantList)) {
            menuDesignPermission.setRelevant(notSameModelRelevantList);
            designPermissions.add(menuDesignPermission);
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
