package cn.bctools.design.menu.service.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.menu.entity.AppMenuType;
import cn.bctools.design.menu.mapper.AppMenuTypeMapper;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.menu.service.AppMenuTypeService;
import cn.bctools.design.project.handler.IJvsDesigner;
import cn.bctools.design.sqlInjector.MapperMethodHandler;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 轻应用菜单目录 服务实现类
 */
@Service
@AllArgsConstructor
public class AppMenuTypeServiceImpl extends ServiceImpl<AppMenuTypeMapper, AppMenuType> implements AppMenuTypeService, IJvsDesigner {
    private final AppMenuService appMenuService;
    private final MapperMethodHandler mapperMethodHandler;

    @Override
    public void updateType(String id, String appId, String oldType, String newType, String icon, String parentId) {
        if (ObjectNull.isNull(parentId)) {
            throw new BusinessException("应用错误或设计不存在");
        }
        List<AppMenuType> menuTypes = list(Wrappers.<AppMenuType>lambdaQuery().eq(AppMenuType::getJvsAppId, appId).orderByDesc(AppMenuType::getSort));
        if (Objects.isNull(menuTypes)) {
            throw new BusinessException("应用错误或设计不存在");
        }
        // 修改目录名校验 true-修改目录名,false-不修改目录名
        boolean modifyType = Boolean.FALSE.equals(oldType.equals(newType));
        // 同级目录不能重名
        if (modifyType) {
            boolean exists = menuTypes.stream()
                    .filter(menuType -> {
                        if (appId.equals(parentId)) {
                            return ObjectNull.isNull(menuType.getParentId());
                        } else {
                            return parentId.equals(menuType.getParentId());
                        }
                    }).anyMatch(menuType -> menuType.getType().equals(newType));
            if (exists) {
                throw new BusinessException("已存在重名目录");
            }
        } else {
            List<AppMenuType> menuTypeList = menuTypes.stream()
                    .filter(menuType -> {
                        if (appId.equals(parentId)) {
                            return ObjectNull.isNull(menuType.getParentId());
                        } else {
                            return parentId.equals(menuType.getParentId());
                        }
                    }).collect(Collectors.toList());
            // 若同级目录中包含当前目录id，则表示为目录内移动，不需要校验是否重名
            boolean noChange = menuTypeList.stream().anyMatch(type -> type.getId().equals(id));
            if (Boolean.FALSE.equals(noChange) && menuTypeList.stream().anyMatch(t -> t.getType().equals(oldType))) {
                throw new BusinessException("已存在重名目录");
            }
        }
        Optional<AppMenuType> appMenuTypeOptional = menuTypes.stream().filter(menuType -> menuType.getId().equals(id)).findFirst();
        if (appMenuTypeOptional.isPresent()) {
            AppMenuType appMenuType = appMenuTypeOptional.get();
            String pid = appId.equals(parentId) ? null : parentId;
            updateById(appMenuType.setType(newType).setIcon(icon).setParentId(pid));
        } else {
            throw new BusinessException("该目录不存在");
        }
    }

    @Override
    public void removeType(String appId, String typeId) {
        // 校验该目录下是否还存在设计数据
        boolean existType = appMenuService.exist(appId, typeId);
        if (existType) {
            throw new BusinessException("该目录下存在设计数据不能删除");
        }
        if (count(Wrappers.<AppMenuType>lambdaQuery().eq(AppMenuType::getJvsAppId, appId)) == 1) {
            throw new BusinessException("至少保留一个目录");
        }
        removeById(typeId);
    }

    @Override
    public List<AppMenuType> getAppAll(String appId) {
        return Optional.ofNullable(list(Wrappers.<AppMenuType>lambdaQuery().eq(AppMenuType::getJvsAppId, appId)))
                .orElseGet(ArrayList::new)
                .stream()
                .sorted(Comparator.comparing(AppMenuType::getSort))
                .collect(Collectors.toList());
    }

    @Override
    public List<AppMenuType> getAppAll(String appId, String appVersion) {
        return Optional.ofNullable(list(Wrappers.<AppMenuType>lambdaQuery()
                        .eq(AppMenuType::getJvsAppId, appId)
                        .eq(ObjectNull.isNotNull(appVersion), AppMenuType::getAppVersion, appVersion))
                )
                .orElseGet(ArrayList::new)
                .stream()
                .sorted(Comparator.comparing(AppMenuType::getSort))
                .collect(Collectors.toList());
    }

    @Override
    public void beforeAppDeleted(String appId) {
        mapperMethodHandler.deletePhysical(this, Wrappers.<AppMenuType>lambdaQuery().eq(AppMenuType::getJvsAppId, appId));
    }
}
