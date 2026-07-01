package cn.bctools.design.menu.util;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.menu.entity.AppMenuType;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 菜单工具
 */
public class MenuUtil {
    private MenuUtil() {
    }

    /**
     * 得到菜单id集合
     *
     * @param menuTypes 目录
     * @param appMenus  菜单
     * @return 排序后的菜单id集合
     */
    public static List<String> getMenuIds(List<AppMenuType> menuTypes, List<AppMenu> appMenus) {
        if (ObjectNull.isNotNull(menuTypes) && ObjectNull.isNotNull(appMenus)) {
            return menuTypes.stream()
                    .flatMap(type ->
                            appMenus.stream()
                                    .filter(m -> type.getId().equals(m.getType()))
                                    .sorted(Comparator.comparing(AppMenu::getSort)))
                    .map(AppMenu::getDesignId)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
