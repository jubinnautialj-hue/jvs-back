package cn.bctools.design.project.service;

import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.project.entity.JvsApp;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Auto Generator
 */
public interface JvsAppService extends IService<JvsApp> {
    /**
     * 发布应用
     *
     * @param appId 应用id
     * @return 修改结果
     */
    boolean deploy(String appId);

    /**
     * 卸载应用
     *
     * @param appId 应用id
     * @return 修改结果
     */
    boolean unload(String appId);

    /**
     * 是否存在指定目录
     *
     * @param appId 应用id
     * @param type  应用目录
     * @return 返回true有
     */
    boolean existType(String appId, String type);

    /**
     * 添加应用目录
     *
     * @param appId    应用id
     * @param type     应用目录
     * @param icon     图标
     * @param parentId 上级目录id
     */
    void addType(String appId, String type, String icon, String parentId);

    /**
     * 删除应用目录
     *
     * @param appId 应用id
     * @param typeId  应用目录id
     */
    void removeType(String appId, String typeId);

    /**
     * 修改应用目录
     *
     * @param id       目录id
     * @param appId    应用id
     * @param oldType  旧应用目录
     * @param newType  新应用目录
     * @param icon     图标
     * @param parentId 上级目录id
     */
    void updateType(String id, String appId, String oldType, String newType, String icon, String parentId);

    /**
     * 获取应用
     *
     * @param appId
     * @return
     */
    JvsApp getAppById(String appId);

    /**
     * 校验用户是否有指定的应用权限
     *
     * @param personnels 指定要校验的权限配置
     * @param userDto    用户
     * @return TRUE-有权限，FALSE-无权限
     */
    Boolean checkRole(List<PersonnelDto> personnels, UserDto userDto);

    /**
     * 查询所有未启用轻应用版本功能的应用id集合
     *
     * @return 应用id集合
     */
    List<String> getAllNoEnableVersionFeatureAppIds();

    /**
     * 应用是否已启用轻应用版本功能
     *
     * @param appId 应用id
     * @return true-已启用轻应用版本功能，false-未启用轻应用版本功能
     */
    Boolean appEnableVersionFeature(String appId);
}
