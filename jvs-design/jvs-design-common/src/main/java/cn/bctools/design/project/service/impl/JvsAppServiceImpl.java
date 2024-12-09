package cn.bctools.design.project.service.impl;

import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.auth.api.enums.PersonnelTypeEnum;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.menu.component.AppMenuHandler;
import cn.bctools.design.menu.entity.AppMenuType;
import cn.bctools.design.menu.service.AppMenuTypeService;
import cn.bctools.design.project.dto.DesignRoleSettingDto;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.mapper.JvsAppMapper;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.sqlInjector.MapperMethodHandler;
import cn.bctools.design.util.DynamicDataUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Auto Generator
 */
@Service
public class JvsAppServiceImpl extends ServiceImpl<JvsAppMapper, JvsApp> implements JvsAppService {

    @Autowired
    AppMenuTypeService appMenuTypeService;
    @Autowired
    AppMenuHandler appMenuHandler;
    @Autowired
    MapperMethodHandler mapperMethodHandler;

    /**
     * 缓存应用是否已启用轻应用版本功能
     * Map<应用id, 是否已启用轻应用版本功能>
     */
    private final ConcurrentHashMap<String, Boolean> appEnableVersionFeatureCache = new ConcurrentHashMap<>();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deploy(String appId) {
        JvsApp jvsApp = this.get(appId);
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId).setJvsAppName(jvsApp.getName()));
        return this.update(Wrappers.<JvsApp>lambdaUpdate().set(JvsApp::getIsDeploy, true).eq(JvsApp::getId, appId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unload(String appId) {
        this.get(appId);
        return this.update(Wrappers.<JvsApp>lambdaUpdate().set(JvsApp::getIsDeploy, false).eq(JvsApp::getId, appId));
    }

    @Override
    public boolean existType(String appId, String type) {
        if (StringUtils.isBlank(appId) || StringUtils.isBlank(type)) {
            return false;
        }
        JvsApp app = this.getOne(Wrappers.<JvsApp>lambdaQuery().select(JvsApp::getId).eq(JvsApp::getId, appId));
        if (Objects.isNull(app)) {
            throw new BusinessException("应用错误或设计不存在");
        }
        AppMenuType appMenuType = appMenuTypeService.getOne(Wrappers.<AppMenuType>lambdaQuery().eq(AppMenuType::getJvsAppId, app.getId()).eq(AppMenuType::getId, type));
        return ObjectNull.isNotNull(appMenuType);
    }

    @Override
    public void addType(String appId, String type, String icon, String parentId) {
        if (StringUtils.isBlank(appId) || StringUtils.isBlank(type)) {
            return;
        }
        JvsApp app = this.getOne(Wrappers.<JvsApp>lambdaQuery().select(JvsApp::getId).eq(JvsApp::getId, appId));
        if (Objects.isNull(app)) {
            throw new BusinessException("应用错误或设计不存在");
        }
        appMenuHandler.addType(appId, type, icon, parentId);
    }

    @Override
    public void removeType(String appId, String typeId) {
        if (StringUtils.isBlank(appId) || StringUtils.isBlank(typeId)) {
            return;
        }
        JvsApp app = this.getOne(Wrappers.<JvsApp>lambdaQuery().select(JvsApp::getId).eq(JvsApp::getId, appId));
        if (Objects.isNull(app)) {
            throw new BusinessException("应用错误或设计不存在");
        }
        appMenuTypeService.removeType(appId, typeId);
    }

    @Override
    public void updateType(String id, String appId, String oldType, String newType, String icon, String parentId) {
        if (StringUtils.isBlank(appId) || StringUtils.isBlank(oldType) || StringUtils.isBlank(newType)) {
            return;
        }
        JvsApp app = this.getOne(Wrappers.<JvsApp>lambdaQuery().select(JvsApp::getId).eq(JvsApp::getId, appId));
        if (Objects.isNull(app)) {
            throw new BusinessException("应用错误或设计不存在");
        }

        appMenuTypeService.updateType(id, appId, oldType, newType, icon, parentId);
    }

    /**
     * 获取应用信息, 并校验是否存在
     *
     * @param appId 应用id
     * @return 应用信息
     */
    private JvsApp get(String appId) {
        if (StringUtils.isBlank(appId)) {
            throw new BusinessException("应用id为空");
        }
        JvsApp app = this.getById(appId);
        if (Objects.isNull(app)) {
            throw new BusinessException("应用错误或设计不存在");
        }
        return app;
    }

    @Override
    public JvsApp getAppById(String appId) {
        return get(appId);
    }

    @Override
    public Boolean checkRole(List<PersonnelDto> personnels, UserDto userDto) {
        return Optional.ofNullable(personnels).orElseGet(ArrayList::new)
                .stream()
                .anyMatch(role -> PersonnelTypeEnum.user.equals(role.getType()) && userDto.getId().equals(role.getId()));
    }

    @Override
    public List<String> getAllNoEnableVersionFeatureAppIds() {
        return list(Wrappers.<JvsApp>lambdaQuery()
                .eq(JvsApp::getEnableVersionFeature, Boolean.FALSE)
        )
                .stream()
                .map(JvsApp::getId)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean appEnableVersionFeature(String appId) {
        Boolean enable = appEnableVersionFeatureCache.get(appId);
        if (ObjectNull.isNull(enable)) {
            JvsApp jvsApp = getAppById(appId);
            enable = jvsApp.getEnableVersionFeature();
            // 只缓存已启用轻应用版本功能的应用
            if (enable) {
                appEnableVersionFeatureCache.put(appId, enable);
            }
        }
        return enable;
    }

    @Override
    public boolean removeById(Serializable id) {
        mapperMethodHandler.deletePhysical(this, Wrappers.<JvsApp>lambdaQuery().eq(JvsApp::getId, id));
        return true;
    }
}
