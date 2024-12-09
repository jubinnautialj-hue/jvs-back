package cn.bctools.design.menu.service.impl;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.menu.mapper.AppMenuMapper;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.menu.util.DesignPermissionUtil;
import cn.bctools.design.project.handler.IJvsDesigner;
import cn.bctools.design.sqlInjector.MapperMethodHandler;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 菜单权限表 服务实现类
 */
@Service
@AllArgsConstructor
public class AppMenuServiceImpl extends ServiceImpl<AppMenuMapper, AppMenu> implements AppMenuService, IJvsDesigner {

    private final MapperMethodHandler mapperMethodHandler;

    @Override
    public void display(String appId, String designId, Boolean mobileDisplay, Boolean pcDisplay) {
        update(Wrappers.<AppMenu>lambdaUpdate()
                .set(ObjectNull.isNotNull(mobileDisplay), AppMenu::getMobileDisplay, mobileDisplay)
                .set(ObjectNull.isNotNull(pcDisplay), AppMenu::getPcDisplay, pcDisplay)
                .eq(AppMenu::getJvsAppId, appId)
                .eq(AppMenu::getDesignId, designId)
        );
    }

    @Override
    public void remove(String appId, String designId) {
        remove(Wrappers.<AppMenu>lambdaQuery().eq(AppMenu::getJvsAppId, appId).eq(AppMenu::getDesignId, designId));
    }

    @Override
    public void updateType(String appId, String designId, String newType, String name) {
        update(Wrappers.<AppMenu>lambdaUpdate()
                .set(AppMenu::getType, newType)
                .set(ObjectNull.isNotNull(name), AppMenu::getName, name)
                .eq(AppMenu::getDesignId, designId)
                .eq(AppMenu::getJvsAppId, appId));
    }


    @Override
    public void update(AppMenu appMenu) {
        update(appMenu, Wrappers.<AppMenu>lambdaUpdate()
                .eq(AppMenu::getDesignId, appMenu.getDesignId()));
    }

    @Override
    public AppMenu getDesignMenu(String designId, String appId) {
        return Optional.ofNullable(getOne(Wrappers.<AppMenu>lambdaQuery()
                .select(AppMenu::getType,
                        AppMenu::getDesignType,
                        AppMenu::getSort,
                        AppMenu::getIcon,
                        AppMenu::getRoleType,
                        AppMenu::getRole,
                        AppMenu::getPermissionJson,
                        AppMenu::getPermission,
                        AppMenu::getPcDisplay,
                        AppMenu::getMobileDisplay)
                .eq(AppMenu::getDesignId, designId)
                .eq(AppMenu::getJvsAppId, appId)))
                .orElseGet(AppMenu::new);
    }

    @Override
    public boolean exist(String appId, String typeId) {
        return count(Wrappers.<AppMenu>lambdaQuery().eq(AppMenu::getJvsAppId, appId).eq(AppMenu::getType, typeId)) > 0;
    }

    @Override
    public List<AppMenu> getAppAll(String appId) {
        return Optional.ofNullable(list(Wrappers.<AppMenu>lambdaQuery()
                        .eq(AppMenu::getJvsAppId, appId)))
                .orElseGet(ArrayList::new)
                .stream()
                .peek(m -> {
                    int sort = Optional.ofNullable(m.getSort()).orElse(0);
                    m.setSort(sort);
                }).collect(Collectors.toList());
    }

    @Override
    public List<AppMenu> getAppAll(String appId, String appVersion) {
        return Optional.ofNullable(list(Wrappers.<AppMenu>lambdaQuery()
                        .eq(AppMenu::getJvsAppId, appId)
                        .eq(ObjectNull.isNotNull(appVersion), AppMenu::getAppVersion, appVersion))
                )
                .orElseGet(ArrayList::new)
                .stream()
                .peek(m -> {
                    int sort = Optional.ofNullable(m.getSort()).orElse(0);
                    m.setSort(sort);
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T> void saveBatchPermission(String appId, List<T> designs) {
        if (ObjectNull.isNull(designs)) {
            return;
        }
        List<AppMenu> appMenus = getAppAll(appId);
        if (ObjectNull.isNull(appMenus)) {
            return;
        }
        Class<?> cls = designs.get(0).getClass();
        Collection<AppMenu> updateAppMenus = null;
        // 列表
        if (CrudPage.class.isAssignableFrom(cls)) {
            Map<String, AppMenu> appMenuMap = appMenus.stream()
                    .filter(m -> DesignType.page.equals(m.getDesignType()))
                    .collect(Collectors.toMap(AppMenu::getDesignId, Function.identity()));
            List<CrudPage> crudPages = (List<CrudPage>) designs;
            crudPages.forEach(page -> {
                if (appMenuMap.containsKey(page.getId())) {
                    appMenuMap.get(page.getId()).setPermission(DesignPermissionUtil.parseDesign(DesignType.page, page.getViewJson()));
                }
            });
            updateAppMenus = appMenuMap.values();
        } else if (FormPo.class.isAssignableFrom(cls)) {
            // 表单
            Map<String, AppMenu> appMenuMap = appMenus.stream()
                    .filter(m -> DesignType.form.equals(m.getDesignType()))
                    .collect(Collectors.toMap(AppMenu::getDesignId, Function.identity()));
            List<FormPo> formPos = (List<FormPo>) designs;
            formPos.forEach(form -> {
                if (appMenuMap.containsKey(form.getId())) {
                    appMenuMap.get(form.getId()).setPermission(DesignPermissionUtil.parseDesign(DesignType.form, form.getViewJson()));
                }
            });
            updateAppMenus = appMenuMap.values();
        }
        if (ObjectNull.isNull(updateAppMenus)) {
            return;
        }
        updateBatchById(updateAppMenus);
    }

    @Override
    public void beforeAppDeleted(String appId) {
        mapperMethodHandler.deletePhysical(this, Wrappers.<AppMenu>lambdaQuery().eq(AppMenu::getJvsAppId, appId));
    }
}
