package cn.bctools.design.project.service.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.project.entity.JvsAppVersion;
import cn.bctools.design.project.entity.enums.AppVersionStatusEnum;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.project.mapper.JvsAppVersionMapper;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.project.service.JvsAppVersionService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 应用版本
 */
@Slf4j
@Service
@AllArgsConstructor
public class JvsAppVersionServiceImpl extends ServiceImpl<JvsAppVersionMapper, JvsAppVersion> implements JvsAppVersionService {

    private final JvsAppService jvsAppService;

    @Override
    public boolean existsVersion(String appId) {
        return baseMapper.exists(Wrappers.<JvsAppVersion>lambdaQuery()
                .eq(JvsAppVersion::getJvsAppId, appId));
    }

    @Override
    public void saveVersion(JvsAppVersion version) {
        if (ObjectNull.isNull(version)) {
            return;
        }
        // 将目标类型已存在的版本修改为历史版本
        update(Wrappers.<JvsAppVersion>lambdaUpdate()
                .set(JvsAppVersion::getVersionStatus, AppVersionStatusEnum.HISTORY)
                .eq(JvsAppVersion::getAffiliationApp, version.getAffiliationApp())
                .eq(JvsAppVersion::getVersionType, version.getVersionType()));
        if (ObjectNull.isNull(version.getId())) {
            save(version);
        } else {
            updateById(version);
        }
    }

    @Override
    public String getUseVersion(String appId) {
        return Optional.ofNullable(getUseAppVersion(appId))
                .map(JvsAppVersion::getAppVersion)
                .orElseGet(()->"");
    }

    @Override
    public JvsAppVersion getUseAppVersion(String appId) {
        return getOne(Wrappers.<JvsAppVersion>lambdaQuery()
                .eq(JvsAppVersion::getJvsAppId, appId)
                .ne(JvsAppVersion::getVersionStatus, AppVersionStatusEnum.HISTORY));
    }

    @Override
    public JvsAppVersion getByTemplateId(String templateId) {
        return getOne(Wrappers.<JvsAppVersion>lambdaQuery()
                .eq(JvsAppVersion::getTemplateId, templateId));
    }

    @Override
    public JvsAppVersion getVersion(String version, AppVersionTypeEnum versionType, String affiliationApp) {
        return getOne(Wrappers.<JvsAppVersion>lambdaQuery()
                .eq(JvsAppVersion::getAffiliationApp, affiliationApp)
                .eq(JvsAppVersion::getAppVersion, version)
                .eq(JvsAppVersion::getVersionType, versionType));
    }

    @Override
    public String getVersionTypeAppId(String affiliationApp, AppVersionTypeEnum versionType) {
        List<JvsAppVersion> appVersionList = list(Wrappers.<JvsAppVersion>lambdaQuery().
                eq(JvsAppVersion::getAffiliationApp, affiliationApp)
                .eq(JvsAppVersion::getVersionType, versionType).last("limit 1"));
        // 修复：正确处理空列表的情况（MyBatis Plus的list()方法返回空列表而不是null）
        if (ObjectNull.isNull(appVersionList) || appVersionList.isEmpty()) {
            log.info("未找到应用版本记录 - affiliationApp: {}, versionType: {}", affiliationApp, versionType);
            return null;
        }
        JvsAppVersion version = appVersionList.get(0);
        log.info("找到应用版本记录 - affiliationApp: {}, versionType: {}, jvsAppId: {}", 
            affiliationApp, versionType, version.getJvsAppId());
        return version.getJvsAppId();
    }

    @Override
    public List<String> getVersionTypeAppIds(AppVersionTypeEnum versionType) {
        return getVersionTypeApps(versionType)
                .stream()
                .map(JvsAppVersion::getJvsAppId)
                .collect(Collectors.toList());
    }

    @Override
    public List<JvsAppVersion> getVersionTypeApps(AppVersionTypeEnum versionType) {
        if (ObjectNull.isNull(versionType)) {
            throw new BusinessException("未指定模式");
        }
        // 查询指定模式下的应用id集合
        List<JvsAppVersion> appVersions = list(Wrappers.<JvsAppVersion>lambdaQuery()
                        .select(JvsAppVersion::getJvsAppId, JvsAppVersion::getAppVersion)
                        .eq(JvsAppVersion::getVersionType, versionType)
                .ne(JvsAppVersion::getVersionStatus, AppVersionStatusEnum.HISTORY));

        // 正式模式下, 找到未启用版本功能的应用加入集合
        if (AppVersionTypeEnum.GA.equals(versionType)) {
            List<JvsAppVersion> noEnableVersionFeatureApps = jvsAppService.getAllNoEnableVersionFeatureAppIds()
                    .stream()
                    .map(appId -> new JvsAppVersion().setJvsAppId(appId))
                    .collect(Collectors.toList());
            appVersions.addAll(noEnableVersionFeatureApps);
        }
        return appVersions;
    }

    @Override
    public Set<String> getVersionTypeAppIdByAffiliationId(String affiliationApp) {
        return list(Wrappers.<JvsAppVersion>lambdaQuery()
                .select(JvsAppVersion::getJvsAppId)
                .eq(JvsAppVersion::getAffiliationApp, affiliationApp))
                .stream()
                .map(JvsAppVersion::getJvsAppId)
                .collect(Collectors.toSet());
    }

    @Override
    public String getAffiliationAppByAppId(String appId) {
        return Optional.ofNullable(getOne(Wrappers.<JvsAppVersion>lambdaQuery()
                        .eq(JvsAppVersion::getJvsAppId, appId)
                        .last("limit 1")))
                .map(JvsAppVersion::getAffiliationApp)
                .orElse(appId);
    }

    @Override
    public Map<AppVersionTypeEnum, Set<String>> groupAppIdByVersionType(Collection<String> appIds) {
        if (ObjectNull.isNull(appIds)) {
            return Collections.emptyMap();
        }
        Map<AppVersionTypeEnum, Set<String>> group = list(Wrappers.<JvsAppVersion>lambdaQuery()
                .in(JvsAppVersion::getJvsAppId, appIds)
                .select(JvsAppVersion::getJvsAppId, JvsAppVersion::getVersionType))
                .stream()
                .collect(Collectors.groupingBy(JvsAppVersion::getVersionType, Collectors.mapping(JvsAppVersion::getJvsAppId, Collectors.toSet())));
        // 不在分组内的应用id，是未启用轻应用版本的应用，默认加入正式组
        Set<String> versionAppIds = group.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        appIds.removeAll(versionAppIds);
        if (ObjectNull.isNotNull(appIds)) {
            Set<String> gaAppIds = Optional.ofNullable(group.get(AppVersionTypeEnum.GA)).orElseGet(HashSet::new);
            gaAppIds.addAll(appIds);
            group.put(AppVersionTypeEnum.GA, gaAppIds);
        }
        return group;
    }
}
