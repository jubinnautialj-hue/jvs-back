package cn.bctools.design.permission.service;

import cn.bctools.design.crud.entity.DesignRole;
import cn.bctools.design.permission.dto.PermissionSettingDto;
import cn.bctools.design.permission.entity.dto.PermissionMemberDto;
import cn.bctools.design.project.dto.DesignRoleSettingDto;

import java.util.List;
import java.util.Map;

/**
 * @author zhuxiaokang
 * 权限服务
 */
public interface DesignPermissionService {

    /**
     * 保存权限
     *
     * @param appId       应用id
     * @param groupId     权限组id
     * @param member      权限成员
     * @param permissions 权限
     */
    void savePermission(String appId, String groupId, PermissionMemberDto member, List<PermissionSettingDto> permissions);

    /**
     * 获取权限
     *
     * @param jvsAppId    应用id
     * @param designId    设计id
     * @param dataModelId 模型id
     * @return 包含作权限和数据权限
     */
    DesignRoleSettingDto getDesignPermission(String jvsAppId, String designId, String dataModelId);

    /**
     * 获取模型数据权限
     *
     * @param dataModelId 模型id
     * @return 模型数据权限
     */
    DesignRoleSettingDto getDataModelPermission(String dataModelId);


    /**
     * 获取数据权限
     *
     * @param designId    设计id
     * @param dataModelId 模型id
     * @return 包括设计的自定义数据权限和模型的设计权限
     */
    DesignRoleSettingDto getDataModelPermission(String designId, String dataModelId);


    /**
     * 获取指定设计的操作权限
     *
     * @param designId 设计id
     * @return 指定设计的操作权限集合
     */
    List<DesignRole> getOperationPermissions(String designId);

    /**
     * 批量获取设计的操作权限
     *
     * @param designIds 设计id集合
     * @return Map<设计id, 权限集合>
     */
    Map<String, List<DesignRole>> getBatchOperationPermission(List<String> designIds);
}
