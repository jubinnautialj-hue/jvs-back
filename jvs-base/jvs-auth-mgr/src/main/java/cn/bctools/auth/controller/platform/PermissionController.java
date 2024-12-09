package cn.bctools.auth.controller.platform;

import cn.bctools.auth.service.PermissionService;
import cn.bctools.auth.service.RoleService;
import cn.bctools.auth.service.TenantService;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.function.Get;
import cn.bctools.gateway.entity.Permission;
import cn.bctools.gateway.entity.TenantPo;
import cn.bctools.gateway.entity.TypeEnum;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yxh
 * @ClassName: MenuController
 * @Description: 菜单控制器
 */
@Slf4j
@AllArgsConstructor
@Api(tags = "资源管理")
@RestController
@RequestMapping("permission")
public class PermissionController {

    RoleService roleService;
    TenantService tenantService;
    PermissionService permissionService;

    @GetMapping("/all")
    @ApiOperation("获取资源")
    public R all() {
        return R.ok(permissionService.list(new LambdaQueryWrapper<Permission>().orderByAsc(Permission::getClientName, Permission::getGroupName)));
    }

    @GetMapping("/list")
    @ApiOperation("获取资源树")
    public R<List<Tree<Object>>> list() {
        //判断是否是主租户用户,如果是,返回所有,如果不是,返回租户资源
        String tenantId = UserCurrentUtils.getCurrentUser().getTenantId();
        TenantPo byId = tenantService.getById(tenantId);
        Map<String, List<Permission>> listMap = permissionService.list(new LambdaQueryWrapper<Permission>()
                        .eq(ObjectNull.isNotNull(byId.getParentId()), Permission::getType, TypeEnum.tenant))
                .stream().collect(Collectors.groupingBy(Permission::getClientName));
        List<Tree<Object>> list = new ArrayList<>();
        for (String s : listMap.keySet()) {

            List<Tree<Object>> children = new ArrayList<>();
            Map<String, List<Permission>> map = listMap.get(s).stream().collect(Collectors.groupingBy(Permission::getGroupName));
            for (String groupName : map.keySet()) {
                List<Tree<Object>> collect = map.get(groupName)
                        .stream()
                        .map(e -> {
                            Tree<Object> objectTree = new Tree<Object>().setName(e.getName()).setId(e.getId());
                            objectTree.putExtra(Get.name(Permission::getRemark), e.getRemark());
                            return objectTree;
                        })
                        .collect(Collectors.toList());

                Tree<Object> tree = new Tree<Object>().setName(groupName).setId(groupName).setChildren(collect);
                children.add(tree);
            }

            Tree<Object> tree = new Tree<Object>().setName(s).setId(s).setChildren(children);
            list.add(tree);
        }
        return R.ok(list);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除资源")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> deletePermissionMenu(@PathVariable String id) {
        permissionService.removeById(id);
        return R.ok();
    }

    @PutMapping
    @ApiOperation("修改资源")
    public R<Boolean> putPermission(@RequestBody @Validated Permission permission) {
        permissionService.updateById(permission);
        return R.ok();
    }

    @PostMapping
    @ApiOperation("添加资源")
    public R<Boolean> permission(@RequestBody @Validated Permission permission) {
        permissionService.save(permission);
        return R.ok();

    }

}
