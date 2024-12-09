package cn.bctools.design.project.service;

import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.JvsAppVersion;
import cn.bctools.design.project.entity.TemplateBo;

import java.util.List;

/**
 * The interface App template data service.
 *
 * @param <T> the type parameter
 * @author zhuxiaokang  应用模板数据服务
 */
public interface AppTemplateDataService<T> {

    /**
     * 获取数据
     *
     * @param jvsAppId 应用id
     * @param ids      id集合
     * @return 数据 list
     */
    default List<T> list(String jvsAppId, List<String> ids) {
        return null;
    }

    /**
     * 获取数据
     *
     * @param jvsApp 应用
     * @param ids    id集合
     * @return 数据 list
     */
    default List<T> list(JvsApp jvsApp, List<String> ids) {
        return null;
    }

    /**
     * 获取数据
     *
     * @param modelIds 模型id集合
     * @param ids      id集合
     * @return 数据 list
     */
    default List<T> list(List<String> modelIds, List<String> ids) {
        return null;
    }

    /**
     * 保存
     *
     * @param existsIds               目标版本已存在映射的设计id集合
     * @param templateBo              新的数据集
     * @param targetVersionTemplateBo 目标版本的数据集
     */
    default void save(List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {

    }

    /**
     * 保存
     *
     * @param jvsApp                  the jvs app
     * @param existsIds               目标版本已存在映射的设计id集合
     * @param templateBo              新的数据集
     * @param targetVersionTemplateBo 目标版本的数据集
     */
    default void save(JvsApp jvsApp, List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {

    }

    /**
     * 保存
     *
     * @param jvsApp                  应用
     * @param targetAppVersion        目标版本
     * @param existsIds               目标版本已存在映射的设计id集合
     * @param templateBo              新的数据集
     * @param targetVersionTemplateBo 目标版本的数据集
     */
    default void save(JvsApp jvsApp, JvsAppVersion targetAppVersion, List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {

    }

    /**
     * 保存设计组件的应用版本号
     *
     * @param appId      应用id
     * @param appVersion 应用版本号
     */
    default void saveAppVersion(String appId, String appVersion) {

    }
}
