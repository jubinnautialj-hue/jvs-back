package cn.bctools.design.project.service;

import cn.bctools.design.project.entity.JvsAppVersion;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhuxiaokang
 */
public interface JvsAppVersionService extends IService<JvsAppVersion> {

    /**
     * 应用是否已有版本
     *
     * @param appId 应用id
     * @return true-有版本，false-无版本
     */
    boolean existsVersion(String appId);

    /**
     * 保存应用的版本
     *
     * @param version 版本信息
     */
    void saveVersion(JvsAppVersion version);

    /**
     * 获取应用正在使用的版本
     *
     * @param appId       应用id
     * @return 版本号
     */
    String getUseVersion(String appId);

    /**
     * 获取应用正在使用的版本
     *
     * @param appId       应用id
     * @return 版本号
     */
    JvsAppVersion getUseAppVersion(String appId);

    /**
     * 根据模板id获取版本
     *
     * @param templateId 模板id
     * @return 版本
     */
    JvsAppVersion getByTemplateId(String templateId);

    /**
     * 获取版本信息
     *
     * @param version        版本号
     * @param versionType    版本类型
     * @param affiliationApp 所属应用唯一标识
     * @return 版本
     */
    JvsAppVersion getVersion(String version, AppVersionTypeEnum versionType, String affiliationApp);

    /**
     * 获取版本类型对应的应用id
     *
     * @param affiliationApp 所属应用唯一标识
     * @param versionType    版本类型
     * @return 应用id
     */
    String getVersionTypeAppId(String affiliationApp, AppVersionTypeEnum versionType);

    /**
     * 获取版本类型对应的所有应用id
     *
     * @param versionType 版本类型
     * @return 应用id集合
     */
    List<String> getVersionTypeAppIds(AppVersionTypeEnum versionType);

    /**
     * 获取版本类型对应的所有应用
     *
     * @param versionType 版本类型
     * @return 应用id集合
     */
    List<JvsAppVersion> getVersionTypeApps(AppVersionTypeEnum versionType);


    /**
     * 根据所属应用唯一标识获取所有版本的应用id
     *
     * @param affiliationApp 所属应用唯一标识
     * @return 应用id集合
     */
    Set<String> getVersionTypeAppIdByAffiliationId(String affiliationApp);

    /**
     * 根据应用id获取所属应用唯一标识
     * @param appId 应用id
     * @return 所属应用唯一标识
     */
    String getAffiliationAppByAppId(String appId);

    /**
     * 应用id按模式分组
     *
     * @param appIds 应用id集合
     * @return Map<模式, 模式下的应用id集合>
     */
    Map<AppVersionTypeEnum, Set<String>> groupAppIdByVersionType(Collection<String> appIds);
}
