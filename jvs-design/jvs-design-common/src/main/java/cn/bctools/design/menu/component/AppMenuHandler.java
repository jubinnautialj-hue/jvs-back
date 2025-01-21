package cn.bctools.design.menu.component;

import cn.bctools.auth.api.enums.PersonnelTypeEnum;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.crud.entity.DesignRole;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.menu.entity.AppMenuType;
import cn.bctools.design.menu.entity.dto.PermissionIdentificationDto;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.menu.service.AppMenuTypeService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author zhuxiaokang
 * 应用菜单操作
 */
@Component
@AllArgsConstructor
public class AppMenuHandler {

    private final AppMenuTypeService appMenuTypeService;
    private final AppMenuService appMenuService;

    /**
     * 目录默认图标
     */
    private static final String TYPE_DEFAULT_ICON = "icon-gengduo";

    /**
     * 修改排序
     *
     * @param appId  应用id
     * @param typeId 目标目录id (目录的父目录id 或 设计所属目录id)
     * @param ids    菜单集合（目录id、设计id）
     */
    public void sort(String appId, String typeId, List<String> ids) {
        if (ObjectNull.isNull(typeId) || ObjectNull.isNull(ids)) {
            return;
        }

        // 修改目录排序
        List<AppMenuType> appMenuTypes = appMenuTypeService.list(Wrappers.<AppMenuType>lambdaQuery().in(AppMenuType::getId, ids).eq(AppMenuType::getJvsAppId, appId));
        if (ObjectNull.isNotNull(appMenuTypes)) {
            if (appMenuTypes.size() != appMenuTypes.stream().map(AppMenuType::getType).distinct().count()) {
                throw new BusinessException("已存在重名目录");
            }
            appMenuTypes.forEach(appMenuType -> {
                appMenuType
                        // 目录id与应用id相同，则设置父级目录为null；否则设置父级目录为目标目录id
                        .setParentId(appId.equals(typeId) ? null : typeId)
                        .setSort(ids.indexOf(appMenuType.getId()));
            });
            appMenuTypeService.updateBatchById(appMenuTypes);
        }

        // 修改设计排序
        List<AppMenu> appMenus = appMenuService.list(Wrappers.<AppMenu>lambdaQuery().eq(AppMenu::getJvsAppId, appId).in(AppMenu::getDesignId, ids));
        if (ObjectNull.isNotNull(appMenus)) {
            appMenus.forEach(appMenu -> {
                appMenu.setType(typeId).setSort(ids.indexOf(appMenu.getDesignId()));
            });
            appMenuService.updateBatchById(appMenus);
        }
    }

    /**
     * 增加目录
     *
     * @param appId    应用id
     * @param type     应用目录
     * @param icon     图标
     * @param parentId 上级目录id
     */
    public void addType(String appId, String type, String icon, String parentId) {
        // 上级目录id为空 或 与应用id相同，则将上级目录id设置为null
        String pid = appId.equals(parentId) || ObjectNull.isNull(parentId) ? null : parentId;
        List<AppMenuType> menuTypes = appMenuTypeService.list(Wrappers.<AppMenuType>lambdaQuery()
                .eq(AppMenuType::getJvsAppId, appId)
                // 上级目录id为空，则查询上级目录为空的目录（兼容旧数据一级目录）
                .isNull(ObjectNull.isNull(pid), AppMenuType::getParentId)
                // 上级目录id不为空，则根据上级目录id查询
                .eq(ObjectNull.isNotNull(pid), AppMenuType::getParentId, pid));
        // 目录已存在，则跳过
        boolean exists = menuTypes.stream().anyMatch(menuType -> menuType.getType().equals(type));
        if (exists) {
            throw new BusinessException("同级目录不能重名");
        }

        // 得到序号
        String typeId = ObjectNull.isNotNull(pid) ? pid : appId;
        int sort = generatorSort(appId, typeId, menuTypes);

        // 保存目录
        AppMenuType newAppMenuType = new AppMenuType()
                .setType(type)
                .setIcon(Optional.ofNullable(icon).orElse(TYPE_DEFAULT_ICON))
                .setJvsAppId(appId)
                .setSort(sort)
                .setParentId(pid);
        appMenuTypeService.save(newAppMenuType);

    }

    /**
     * 添加菜单
     *
     * @param designType  设计类型
     * @param designId    设计id
     * @param appId       应用id
     * @param dataModelId 模型id
     * @param name        设计名
     * @param typeId      目录id (为空-不是菜单；不为空-目录id可能是应用id，也可能是目录id)
     */
    public void addMenu(DesignType designType, String designId, String appId, String dataModelId, String name, String typeId) {
        int sort = 0;
        // 目录不为空，得到新的序号
        if (ObjectNull.isNotNull(typeId)) {
            List<AppMenuType> menuTypes = appMenuTypeService.list(Wrappers.<AppMenuType>lambdaQuery()
                    .eq(AppMenuType::getJvsAppId, appId)
                    // 应用id与目录id相同，表示菜单与一级目录同级，而一级目录没有上级目录id，所以查询上级目录id为空的目录
                    .isNull(appId.equals(typeId), AppMenuType::getParentId)
                    // 应用id与目录id不同，表示添加菜单到指定目录下，所以查询上级目录为typeId的目录
                    .eq(Boolean.FALSE.equals(appId.equals(typeId)), AppMenuType::getParentId, typeId));
            sort = generatorSort(appId, typeId, menuTypes);
        }

        // 保存
        AppMenu appMenu = new AppMenu()
                .setDesignType(designType)
                .setDesignId(designId)
                .setJvsAppId(appId)
                .setDataModelId(dataModelId)
                .setName(name)
                .setType(typeId)
                .setSort(sort)
                .setRole(JSONArray.of(JSONObject.parseObject(JSON.toJSONString(new DesignRole().setPersonType(PersonnelTypeEnum.all)))))
                .setRoleType(Boolean.TRUE)
                .setPermissionJson(new JSONArray())
                .setPermission(new PermissionIdentificationDto());
        appMenuService.save(appMenu);
    }

    /**
     * 根据设计id删除菜单
     *
     * @param designIds 设计id集合
     */
    public void removeMenuByDesignIds(List<String> designIds) {
        if (ObjectNull.isNull(designIds)) {
            return;
        }
        appMenuService.remove(Wrappers.<AppMenu>lambdaQuery().in(AppMenu::getDesignId, designIds));
    }


    /**
     * 生成序号
     *
     * @param appId     应用id
     * @param typeId    目录id
     * @param menuTypes 目录集合
     * @return 序号
     */
    private Integer generatorSort(String appId, String typeId, List<AppMenuType> menuTypes) {
        // 得到序号
        List<AppMenu> menus = appMenuService.list(Wrappers.<AppMenu>lambdaQuery()
                .select(AppMenu::getSort)
                .eq(AppMenu::getJvsAppId, appId)
                .eq(AppMenu::getType, typeId));
        Optional<Integer> optionalMaxSort = Stream.of(menuTypes.stream().map(AppMenuType::getSort), menus.stream().map(AppMenu::getSort))
                .flatMap(sortNum -> sortNum)
                .max(Integer::compareTo);
        int sort = 0;
        if (optionalMaxSort.isPresent()) {
            sort = optionalMaxSort.get() + 1;
        }
        return sort;
    }
}
