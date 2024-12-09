package cn.bctools.design.permission;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.crud.entity.DesignRole;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.util.RoleUtils;
import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.menu.util.DesignPermissionUtil;
import cn.bctools.design.permission.service.PermissionCompatibleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Api(tags = "统一权限")
@RestController
@RequestMapping("/base/app/design/permission")
@AllArgsConstructor
public class PermissionBaseController {

    private final AppMenuService appMenuService;
    private final PermissionCompatibleService permissionCompatibleService;

    @ApiOperation("获取自定义页面可用权限")
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
                .stream().flatMap(op -> op.keySet().stream()).collect(Collectors.toSet());
        return R.ok(permit.stream().filter(urlOperationKeys::contains).collect(Collectors.toSet()));
    }

}
