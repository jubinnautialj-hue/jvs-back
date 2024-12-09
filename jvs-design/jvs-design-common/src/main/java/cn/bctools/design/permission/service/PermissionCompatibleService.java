package cn.bctools.design.permission.service;

import cn.bctools.design.crud.entity.DesignRole;
import cn.bctools.design.project.dto.DesignRoleSettingDto;

import java.util.List;

/**
 * @author zhuxiaokang
 * 兼容新旧版本权限功能
 */
public interface PermissionCompatibleService {

    /**
     * 获取设计的权限
     *
     * @param appId       应用id
     * @param designId    设计id
     * @param dataModelId 模型id
     * @return 包含作权限和数据权限
     */
    DesignRoleSettingDto getDesignPermission(String appId, String designId, String dataModelId);

    /**
     * 获取指定设计的操作权限
     *
     * @param appId    应用id
     * @param designId 设计id
     * @return 指定设计的操作权限集合
     */
    List<DesignRole> getOperationPermissions(String appId, String designId);



    /**
     * 获取数据权限
     *
     * @param dataModelId 数据模型id
     * @param pageDesignId 设计id
     * @return 权限
     */
    DesignRoleSettingDto getRoleSetting(String dataModelId, String pageDesignId);

}
