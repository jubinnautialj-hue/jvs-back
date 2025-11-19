package cn.bctools.design.permission;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.crud.entity.DesignRole;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.util.RoleUtils;
import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.menu.util.DesignPermissionUtil;
import cn.bctools.design.permission.dto.PermissionEndpoint;
import cn.bctools.design.permission.service.DesignPermissionService;
import cn.bctools.design.permission.service.PermissionCompatibleService;

import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.util.CurrentAppUtils;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Tag(name = "统一权限")
@RestController
@RequestMapping("/base/app/design/permission")
@AllArgsConstructor
public class PermissionBaseController {

    private final DesignPermissionService designPermissionService;
    private final AppMenuService appMenuService;
    private final PermissionCompatibleService permissionCompatibleService;

    @Operation(summary = "获取自定义页面的权限信息")
    @GetMapping("/{appIdentification}")
    public R get(@PathVariable("appIdentification") String appIdentification) {
        Set<String> operations = new HashSet<>();
        //根据这个应用标识获取
        JvsApp app = CurrentAppUtils.getApp();
        Map<String, AppMenu> menuMap = appMenuService.list(new LambdaQueryWrapper<AppMenu>().eq(AppMenu::getJvsAppId, app.getId()).eq(AppMenu::getDesignType, DesignType.URL).isNotNull(AppMenu::getPermission))
                .stream().collect(Collectors.toMap(AppMenu::getDesignId, Function.identity()));
        Map<String, List<DesignRole>> operationPermissionMap = designPermissionService.getBatchOperationPermission(menuMap.keySet().stream().collect(Collectors.toList()));
        operationPermissionMap.keySet().forEach(e -> {
            List<DesignRole> roles = operationPermissionMap.get(e);
            if (RoleUtils.checkAppDesignPermission(UserCurrentUtils.getUserId(), app)) {
                for (DesignRole role : roles) {
                    operations.addAll(role.getOperation());
                }
            } else if (RoleUtils.hasPermit(roles)) {
                for (DesignRole role : roles) {
                    operations.addAll(RoleUtils.getPermitOperation(role));
                }
            }
        });
        //根据标识获取有哪些权限
        return R.ok(operations);
    }

    @Operation(summary = "获取自定义页面可用权限")
    @GetMapping("/{appId}/url/{designId}")
    public R<Set<String>> detail(@PathVariable String appId, @PathVariable String designId) {
        //判断按钮权限进行解析排除
        List<DesignRole> designRoles = permissionCompatibleService.getOperationPermissions(appId, designId);
        if (ObjectNull.isNotNull(designRoles)) {
            boolean check = RoleUtils.hasPermit(designRoles);
            if (!check) {
                return R.failed("没有权限请联系管理员");
            }
        }
        Set<String> permit = RoleUtils.getPermitOperation(designRoles, Collections.emptyList());
        if (ObjectNull.isNull(permit)) {
            return R.ok();
        }
        AppMenu appMenu = appMenuService.getDesignMenu(designId, appId);
        if (Boolean.FALSE.equals(DesignType.URL.equals(appMenu.getDesignType()))) {
            throw new BusinessException("自定义页面不存在");
        }
        Set<String> urlOperationKeys = DesignPermissionUtil.parseDesign(appMenu)
                .getUrlOperation()
                .stream().map(PermissionEndpoint::getPermission).collect(Collectors.toSet());
        return R.ok(permit.stream().filter(urlOperationKeys::contains).collect(Collectors.toSet()));
    }

}
