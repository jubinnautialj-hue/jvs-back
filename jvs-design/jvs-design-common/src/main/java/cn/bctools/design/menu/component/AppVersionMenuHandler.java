package cn.bctools.design.menu.component;

import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.menu.entity.AppMenuType;
import cn.bctools.design.menu.entity.dto.PermissionIdentificationDto;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.menu.service.AppMenuTypeService;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.project.service.JvsAppVersionService;
import cn.bctools.design.util.ModeUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zhuxiaokang
 * 应用版本菜单
 */
@Component
@AllArgsConstructor
public class AppVersionMenuHandler {
    private final AppMenuService appMenuService;
    private final AppMenuTypeService appMenuTypeService;
    private final JvsAppVersionService appVersionService;

    /**
     * 获取应用当前版本的菜单目录
     *
     * @param appId 应用id
     * @return 应用当前版本的菜单
     */
    public List<AppMenuType> getAppVersionMenuType(String appId) {
        // 获取应用当前使用的版本（若当前是开发模式，不用获取版本）
        String appVersion = null;
        if (Boolean.FALSE.equals(AppVersionTypeEnum.DEV.equals(ModeUtils.getMode()))) {
            appVersion = appVersionService.getUseVersion(appId);
        }
        return appMenuTypeService.getAppAll(appId, appVersion);
    }


    /**
     * 获取应用当前版本的菜单
     *
     * @param appId 应用id
     * @return 应用当前版本的菜单
     */
    public List<AppMenu> getAppVersionMenu(String appId) {
        // 获取应用当前使用的版本（若当前是开发模式，不用获取版本）
        String appVersion = null;
        if (Boolean.FALSE.equals(AppVersionTypeEnum.DEV.equals(ModeUtils.getMode()))) {
            appVersion = appVersionService.getUseVersion(appId);
        }
        return appMenuService.getAppAll(appId, appVersion);
    }

    /**
     * 修改设计操作权限标识
     *
     * @param designId   设计id
     * @param permission 权限标识
     */
    public void updateOperation(String designId, PermissionIdentificationDto permission) {
        AppMenu appMenu = new AppMenu()
                .setDesignId(designId)
                .setPermission(permission);
        appMenuService.update(appMenu);
    }

}
